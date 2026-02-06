package me.sfoow.prokits.Ect;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions;

import java.io.*;
import java.util.*;

public class YamlManager {

    private final File file;
    private Map<String, Object> data;
    private final Yaml yaml;
    private boolean dirty = false;

    public YamlManager(String path) {
        this.file = new File(path);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        this.yaml = new Yaml(options);

        load();
    }

    /** Loads YAML from disk */
    public void load() {
        try {
            if (!file.exists()) {
                file.createNewFile();
                data = new LinkedHashMap<>();
                save();
                return;
            }

            try (FileInputStream fis = new FileInputStream(file)) {
                Object loaded = yaml.load(fis);
                if (loaded instanceof Map) {
                    data = (Map<String, Object>) loaded;
                } else {
                    data = new LinkedHashMap<>();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            data = new LinkedHashMap<>();
        }
        dirty = false;
    }

    public int getIntOrDefault(String path, int def) {
        Object v = get(path);
        return v instanceof Number ? ((Number) v).intValue() : def;
    }

    /** Saves YAML to disk ONLY if modified */
    public void save() {
        if (!dirty) return;

        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(data, writer);
            dirty = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Internal: Navigates to a nested map path like "a.b.c" */
    private Map<String, Object> getSection(String path, boolean create) {
        String[] parts = path.split("\\.");
        Map<String, Object> section = data;

        for (int i = 0; i < parts.length - 1; i++) {
            Object next = section.get(parts[i]);

            if (!(next instanceof Map)) {
                if (!create) return null;

                Map<String, Object> newSection = new LinkedHashMap<>();
                section.put(parts[i], newSection);
                section = newSection;
                dirty = true;
            } else {
                section = (Map<String, Object>) next;
            }
        }
        return section;
    }

    /** Sets a value but DOES NOT save immediately */
    public void set(String path, Object value) {
        Map<String, Object> section = getSection(path, true);
        String key = path.substring(path.lastIndexOf('.') + 1);

        section.put(key, value);
        dirty = true;
    }

    /** Sets a value and saves immediately (legacy behavior) */
    public void setAndSave(String path, Object value) {
        set(path, value);
        save();
    }

    /** Gets a value at "path.key" */
    public Object get(String path) {
        Map<String, Object> section = getSection(path, false);
        if (section == null) return null;

        String key = path.substring(path.lastIndexOf('.') + 1);
        return section.get(key);
    }

    public String getString(String path) {
        Object v = get(path);
        return v != null ? v.toString() : null;
    }

    public Integer getInt(String path) {
        Object v = get(path);
        return v instanceof Number ? ((Number) v).intValue() : null;
    }

    public Boolean getBoolean(String path) {
        Object v = get(path);
        return v instanceof Boolean ? (Boolean) v : null;
    }

    public List<String> getStringList(String path) {
        Object v = get(path);
        if (v instanceof List<?>) {
            List<String> list = new ArrayList<>();
            for (Object o : (List<?>) v) {
                list.add(String.valueOf(o));
            }
            return list;
        }
        return Collections.emptyList();
    }

    public Byte getByte(String path) {
        Object v = get(path);
        return v instanceof Number ? ((Number) v).byteValue() : null;
    }

    public Map<String, Object> getMap(String path) {
        Object v = get(path);
        return v instanceof Map ? (Map<String, Object>) v : null;
    }
}
