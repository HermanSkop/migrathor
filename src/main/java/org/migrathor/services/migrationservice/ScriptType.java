package org.migrathor.services.migrationservice;

public enum ScriptType {
    DO {
        public String toString() {
            return "v";
        }
    },

    UNDO {
        public String toString() {
            return "u";
        }
    }
}
