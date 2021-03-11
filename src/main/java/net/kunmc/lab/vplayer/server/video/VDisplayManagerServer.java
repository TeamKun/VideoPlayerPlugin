package net.kunmc.lab.vplayer.server.video;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.kunmc.lab.vplayer.ProxyServer;
import net.kunmc.lab.vplayer.common.data.DataSerializer;
import net.kunmc.lab.vplayer.common.model.DisplayManagaer;
import net.kunmc.lab.vplayer.common.model.PlayState;
import net.kunmc.lab.vplayer.common.model.Quad;
import net.kunmc.lab.vplayer.common.patch.VideoPatch;
import net.kunmc.lab.vplayer.common.patch.VideoPatchOperation;
import net.kunmc.lab.vplayer.common.video.VDisplay;
import net.kunmc.lab.vplayer.common.video.VDisplayManager;
import net.kunmc.lab.vplayer.server.patch.VideoPatchSendEventServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static net.kunmc.lab.vplayer.VideoPlayer.MODID;

public class VDisplayManagerServer implements DisplayManagaer<String, VDisplay> {
    private static final String DATA_NAME = MODID + "_displays";

    private final File nbt;
    private final Map<String, UUID> displayNames = new ConcurrentHashMap<>();
    private final VDisplayManager manager = new VDisplayManager();

    public VDisplayManagerServer(File file) {
        nbt = file;
    }

    private static class DataModel {
        public List<DisplayModel> displays = new ArrayList<>();

        public static class DisplayModel {
            public String name;
            public VideoPatch data;
        }
    }

    public void read() {
        clear();

        if (!nbt.exists())
            return;

        DataModel dataModel;
        try (JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(nbt), StandardCharsets.UTF_8))) {
            dataModel = DataSerializer.decode(reader, DataModel.class);
        } catch (IOException e) {
            return;
        }

        if (dataModel == null)
            return;

        List<DataModel.DisplayModel> list = dataModel.displays;

        if (list == null)
            return;

        list.forEach(node -> {
            String name = node.name;
            VideoPatch patch = node.data;
            if (name == null || name.isEmpty() || patch == null)
                return;
            displayNames.put(name, patch.getId());
            VDisplay display = manager.create(patch.getId());
            display.setQuad(patch.getQuad());
            display.dispatchState(patch.getState());
        });
    }

    public void write() {
        DataModel dataModel = new DataModel();
        List<DataModel.DisplayModel> list = dataModel.displays;
        displayNames.forEach((name, id) -> {
            Optional.ofNullable(manager.get(id)).ifPresent(d -> {
                DataModel.DisplayModel tag = new DataModel.DisplayModel();
                tag.name = name;
                tag.data = new VideoPatch(d.getUUID(), d.getQuad(), d.fetchState());
                list.add(tag);
            });
        });

        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(nbt), StandardCharsets.UTF_8))) {
            DataSerializer.encode(dataModel, writer);
        } catch (IOException ignored) {
        }
    }

    private void sendToClient(VideoPatchOperation operation, List<VideoPatch> patches) {
        ProxyServer.getServer().getPluginManager().callEvent(new VideoPatchSendEventServer(operation, patches));
    }

    private void deleteDisplay(UUID e) {
        if (manager.get(e) != null) {
            manager.destroy(e);

            sendToClient(VideoPatchOperation.DELETE, Collections.singletonList(new VideoPatch(e, null, null)));
        }
    }

    // Operation
    @Nonnull
    @Override
    public VDisplay create(String name) {
        UUID uuid = UUID.randomUUID();
        deleteDisplay(uuid);
        VDisplay display = manager.create(uuid);
        Optional.ofNullable(displayNames.put(name, uuid))
                .ifPresent(this::deleteDisplay);

        sendToClient(VideoPatchOperation.UPDATE, Collections.singletonList(new VideoPatch(uuid, null, display.fetchState())));

        return display;
    }

    @Override
    public void destroy(String name) {
        Optional.ofNullable(displayNames.remove(name))
                .ifPresent(this::deleteDisplay);
    }

    @Override
    public void clear() {
        manager.clear();
        displayNames.clear();
    }

    @Override
    public VDisplay get(String name) {
        return Optional.ofNullable(displayNames.get(name))
                .map(manager::get).orElse(null);
    }

    public VDisplay get(UUID id) {
        return manager.get(id);
    }

    public void dispatchState(String name, Function<PlayState, PlayState> dispatch) {
        Optional.ofNullable(displayNames.get(name))
                .flatMap(e -> Optional.ofNullable(manager.get(e)))
                .ifPresent(e -> {
                    e.dispatchState(dispatch.apply(e.fetchState()));

                    sendToClient(VideoPatchOperation.UPDATE, Collections.singletonList(new VideoPatch(e.getUUID(), e.getQuad(), e.fetchState())));
                });
    }

    public void setQuad(String name, @Nullable Quad quad) {
        Optional.ofNullable(displayNames.get(name))
                .flatMap(e -> Optional.ofNullable(manager.get(e)))
                .ifPresent(e -> {
                    e.setQuad(quad);

                    sendToClient(VideoPatchOperation.UPDATE, Collections.singletonList(new VideoPatch(e.getUUID(), e.getQuad(), e.fetchState())));
                });
    }

    public List<String> listNames() {
        return new ArrayList<>(displayNames.keySet());
    }

    @Override
    public List<VDisplay> list() {
        return manager.list();
    }

    // WorldSavedData methods
    public static VDisplayManagerServer get(File dataFolder) {
        return new VDisplayManagerServer(new File(dataFolder, DATA_NAME + ".json"));
    }
}