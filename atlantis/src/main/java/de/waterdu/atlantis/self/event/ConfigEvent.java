package de.waterdu.atlantis.self.event;

import de.waterdu.atlantis.file.ClassHolder;
import de.waterdu.atlantis.file.Config;
import de.waterdu.atlantis.file.datatypes.Configuration;
import de.waterdu.atlantis.file.storage.Storage;
import net.neoforged.bus.api.Event;

public abstract class ConfigEvent extends Event {

    private final Config<? extends Configuration> config;

    private ConfigEvent(Config<? extends Configuration> config) {
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    public <T extends Configuration> Config<T> config() {
        return (Config<T>) this.config;
    }

    public static class Register extends ConfigEvent {

        private final String modID;
        private String path;
        private final ClassHolder<? extends Configuration> classes;
        private final Configuration instance;
        private final Storage storage;

        public Register(Config<? extends Configuration> config, String path) {
            super(config);
            this.modID = config.owner();
            this.path = path;
            this.classes = config.classes();
            this.instance = config.get();
            this.storage = config.storage();
        }

        public String mod() {
            return this.modID;
        }

        public String path() {
            return this.path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @SuppressWarnings("unchecked")
        public <T extends Configuration> ClassHolder<T> classes() {
            return (ClassHolder<T>) this.classes;
        }

        @SuppressWarnings("unchecked")
        public <T extends Configuration> T instance() {
            return (T) this.instance;
        }

        public Storage storage() {
            return this.storage;
        }
    }

    public static class Read extends ConfigEvent {

        private final Configuration object;

        public Read(Config<? extends Configuration> config, Configuration object) {
            super(config);
            this.object = object;
        }

        @SuppressWarnings("unchecked")
        public <T extends Configuration> T object() {
            return (T) this.object;
        }
    }

    public static class Write extends ConfigEvent {

        private final Configuration object;

        public Write(Config<? extends Configuration> config, Configuration object) {
            super(config);
            this.object = object;
        }

        @SuppressWarnings("unchecked")
        public <T extends Configuration> T object() {
            return (T) this.object;
        }
    }

    public static class Delete extends ConfigEvent {

        private final Configuration object;

        public Delete(Config<? extends Configuration> config, Configuration object) {
            super(config);
            this.object = object;
        }

        @SuppressWarnings("unchecked")
        public <T extends Configuration> T object() {
            return (T) this.object;
        }
    }
}
