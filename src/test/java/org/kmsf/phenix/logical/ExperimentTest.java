package org.kmsf.phenix.logical;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.Join;
import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Table;
import org.kmsf.phenix.function.Functions;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExperimentTest {

    @Test
    public void aggregatorQuery() throws ScopeException {
        Table tCustomer = new Table("customer");
        Entity customer = new Entity("customer", tCustomer);
        Attribute customerName = customer.attribute("name").alias("customerName");
        Table tTransaction = new Table("transaction");
        Entity transaction = new Entity("transaction", tTransaction);
        Attribute totalAmount = transaction.attribute("totalAmount", Functions.SUM(tTransaction.column("amount")));
        Join join = new Join(customer, Functions.EQUALS(tCustomer.column("ID"), tTransaction.column("CUST_ID_FK")));
        Attribute transactionCustomer = transaction.attribute("transactions", join);
        assertEquals("SELECT SUM(t.'amount') AS totalAmount, c.*, c.'name' AS customerName FROM 'transaction' t INNER JOIN 'customer' c ON c.'ID'=t.'CUST_ID_FK' GROUP BY c.'name'",
                new Query(transaction).select(totalAmount).select(transactionCustomer).groupBy(customerName).print());
    }

    @Test
    public void aggregatorAndJoinQuery() throws ScopeException {
        Table tCustomer = new Table("customer");
        Entity customer = new Entity("customer", tCustomer);
        Attribute customerName = customer.attribute("name").alias("customerName");
        Table tTransaction = new Table("transaction");
        Entity transaction = new Entity("transaction", tTransaction);
        Attribute totalAmount = transaction.attribute("totalAmount", Functions.SUM(tTransaction.column("amount")));
        Join join = new Join(customer, Functions.EQUALS(tCustomer.column("ID"), tTransaction.column("CUST_ID_FK")));
        Attribute transactionCustomer = transaction.attribute("transactions", join);
        assertEquals("SELECT SUM(t.'amount') AS totalAmount, c.'name' AS customerName FROM 'transaction' t INNER JOIN 'customer' c ON c.'ID'=t.'CUST_ID_FK' GROUP BY c.'name'",
                new Query(transaction).select(totalAmount).groupBy(Attribute.APPLY(transactionCustomer, customerName)).print());
    }

    @Test
    public void checkingApplyArgumentsQuery() throws ScopeException {
        Table tCustomer = new Table("customer");
        Entity customer = new Entity("customer", tCustomer);
        Attribute customerName = customer.attribute("name").alias("customerName");
        Table tTransaction = new Table("transaction");
        Entity transaction = new Entity("transaction", tTransaction);
        Attribute totalAmount = transaction.attribute("totalAmount", Functions.SUM(tTransaction.column("amount")));
        Join join = new Join(customer, Functions.EQUALS(tCustomer.column("ID"), tTransaction.column("CUST_ID_FK")));
        Attribute transactionCustomer = transaction.attribute("transactions", join);
        assertThrows(ScopeException.class, () -> new Query(transaction).select(totalAmount).groupBy(Attribute.APPLY(totalAmount, customerName)).print());
        assertEquals("SELECT SUM(t.'amount') AS totalAmount, c.'name' AS customerName FROM 'transaction' t INNER JOIN 'customer' c ON c.'ID'=t.'CUST_ID_FK' GROUP BY c.'name'",
                new Query(transaction).select(totalAmount).groupBy(Attribute.APPLY(transactionCustomer, customerName)).print());
    }

    @Test
    void checkTableEntityJoinEquals() {
        Table tCustomer = new Table("customer");
        Entity customer = new Entity("customer", tCustomer);
        Table tTransaction = new Table("transaction");
        Entity transaction = new Entity("transaction", tTransaction);
        Join join = new Join(customer, Functions.EQUALS(tCustomer.column("ID"), tTransaction.column("CUST_ID_FK")));
        assertEquals(tCustomer, customer);
        assertEquals(customer, tCustomer);
        assertEquals(customer, join);
        assertEquals(join, customer);
        assertEquals(tCustomer, join);
        assertEquals(join, tCustomer);
        assertNotEquals(tCustomer, tTransaction);
        assertNotEquals(customer, transaction);
        assertNotEquals(customer, tTransaction);
        assertNotEquals(tTransaction, customer);
        assertNotEquals(transaction, join);
        assertNotEquals(tTransaction, join);
        assertNotEquals(join, transaction);
        assertNotEquals(join, tTransaction);
    }
}
