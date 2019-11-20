package org.kmsf.phenix.database;

import org.kmsf.phenix.function.Function;

import java.util.Optional;

public abstract class Selector extends Function {

    public abstract Optional<String> getDefaultAlias();
}
