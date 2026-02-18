package com.oxywire.oxytowns.storage;

import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.entities.impl.town.Town;
import com.oxywire.oxytowns.utils.Json;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TownStorageManager {

    private final File dataFile;
    private final File backupFile;

    public TownStorageManager(final OxyTownsPlugin plugin) {
        this.dataFile = new File(plugin.getDataFolder(), "towns");
        this.backupFile = new File(plugin.getDataFolder(), "towns_backups");
        if (!this.dataFile.exists()) {
            this.dataFile.mkdirs();
        }
        if (!this.backupFile.exists()) {
            this.backupFile.mkdirs();
        }
    }

    /**
     * Handles getting all the towns from the file system
     *
     * @return the list of towns from the file system
     */
    public List<Town> getAll() {
        final List<Town> towns = new ArrayList<>();
        final File[] townFiles = this.dataFile.listFiles();
        if (townFiles == null) {
            return towns;
        }

        Logger logger = OxyTownsPlugin.get().getSLF4JLogger();
        for (final File townFile : townFiles) {
            if (!townFile.isFile() || !townFile.getName().endsWith(".json")) {
                logger.warn("Skipping non town file under towns data folder: {}", townFile.getName());
                continue;
            }

            try(BufferedReader reader = Files.newBufferedReader(townFile.toPath(), StandardCharsets.UTF_8)) {
                final Town town = Json.GSON.fromJson(reader, Town.class);
                towns.add(town);
            } catch (final Exception e) {
                logger.error("Failed to load town from file: {}", townFile.getName(), e);
            }
        }
        return towns;
    }

    /**
     * Handles saving a town to the file system
     *
     * @param entity the town to save to the file system
     */
    public void save(final Town entity) {
        CompletableFuture.runAsync(() -> this.unload(entity));
    }

    /**
     * Called when the server is shutting down. Can't schedule new task
     *
     * @param entity the town to save
     */
    public void unload(final Town entity) {
        final File file = new File(this.dataFile, entity.getTownId().toString() + ".json");
        try {
            Files.writeString(file.toPath(), Json.GSON.toJson(entity, Town.class));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Used to backup a town for any reason
    *
     * @param entity the town to backup
     * @param type  the type of backup (the subfolder name)
     */
    public void backup(final Town entity, final String type) {
        final File backupDir = new File(this.backupFile, type);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        final File file = new File(backupDir, entity.getTownId().toString() + ".json");
        try {
            Files.writeString(file.toPath(), Json.GSON.toJson(entity, Town.class));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Handles deleting a town from the file system
     *
     * @param entity the town to save from the file system
     */
    public void delete(final Town entity) {
        CompletableFuture.runAsync(() -> new File(this.dataFile, entity.getTownId().toString() + ".json").delete());
    }
}
