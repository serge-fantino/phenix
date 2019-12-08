package org.kmsf.phenix.sql;

import org.kmsf.phenix.database.View;
import org.kmsf.phenix.database.ScopeException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A scope maintain a linked stack of references
 */
public class Scope implements Iterable<Mapping> {

    private static Logger logger = Logger.getLogger(Scope.class.getName());

    private View container = null;
    private Optional<Scope> parent = Optional.empty();
    private Optional<Mapping> mapping = Optional.empty();
    private int deepth = 0;

    protected Scope() {
        // for testing only
    }

    public Scope(View container) {
        this.container = container;
    }

    public Scope(Scope parent) {
        this.deepth = parent.deepth+1;
        this.parent = Optional.of(parent);
        this.container = parent.container;
    }

    public Scope(Scope parent, View view, String alias) {
        this.container = parent.container;
        this.deepth = parent.deepth+1;
        this.parent = Optional.of(parent);
        this.mapping = Optional.of(new Mapping(this, view, alias));
    }

    public View getContainer() {
        return container;
    }

    public Scope add(View view, String alias) {
        var scope = new Scope(this, view, alias);
        logger.info("pushing "+view+" as "+alias+" in scope lvl#"+scope.deepth);
        return scope;
    }

    public Mapping resolves(View view) throws ScopeException {
        var mapping = safeResolves(view);
        if (mapping.isEmpty())
            throw new ScopeException("undefined reference to {" + view + "} in scope " + this.toString());
        return mapping.get();
    }

    public boolean canResolves(View view) {
        return safeResolves(view).isPresent();
    }

    protected Optional<Mapping> safeResolves(View view) {
        for (var mapping : this) {
            if (mapping.getReference().isCompatibleWith(view)) {
                logger.log(Level.INFO, "getting alias for "+view+" as '"+mapping.getAlias()+"' from "+mapping.getReference()+" in scope level "+deepth);
                return Optional.of(mapping);
            }
        }
        return Optional.empty();
    }

    public boolean contains(View view) {
        for (var mapping : this) {
            if (mapping.getReference().equals(view)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return parent.isEmpty() && mapping.isEmpty();
    }

    /**
     * return the alias mapping
     * @param alias
     * @return the associated mapping or throw ScopeException
     */
    public Mapping forAlias(String alias) throws ScopeException {
        for (var mapping : this) {
            if (mapping.getAlias().equals(alias)) return mapping;
        }
        throw new ScopeException("cannot find alias '"+alias+"' in scope "+ this);
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("[SCOPE lvl#"+deepth+" for "+(container!=null?container:"???")+" = ");
        result.append("[");
        this.forEach(mapping -> result.append(mapping.toString()).append(","));
        result.append("]");
        return result.toString();
    }

    @Override
    public Iterator<Mapping> iterator() {
        return new Iterator<Mapping>() {
            private Scope current = Scope.this;
            @Override
            public boolean hasNext() {
                return current.mapping.isPresent();
            }

            @Override
            public Mapping next() {
                var next = current.mapping.get();
                current = current.parent.orElseThrow(IllegalStateException::new);
                return next;
            }
        };
    }
}
