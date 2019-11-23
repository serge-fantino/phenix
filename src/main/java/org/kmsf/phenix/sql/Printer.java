package org.kmsf.phenix.sql;

import org.kmsf.phenix.database.ScopeException;

public interface Printer {

    PrintResult print(PrintResult result) throws ScopeException;

}
