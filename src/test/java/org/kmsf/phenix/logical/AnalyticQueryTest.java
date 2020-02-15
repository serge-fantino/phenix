package org.kmsf.phenix.logical;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.Join;
import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Table;
import org.kmsf.phenix.algebra.Functions;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * this is a test case to experiment how to use groupBy and Join
 */
public class AnalyticQueryTest {

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
        assertEquals("SELECT SUM(t.amount) AS totalAmount, c.*, c.name AS customerName FROM transaction t INNER JOIN customer c ON c.ID=t.CUST_ID_FK GROUP BY c.name",
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
        assertEquals("SELECT SUM(t.amount) AS totalAmount, c.name AS customerName FROM transaction t INNER JOIN customer c ON c.ID=t.CUST_ID_FK GROUP BY c.name",
                new Query(transaction).select(totalAmount).groupBy(transactionCustomer.apply(customerName)).print());
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
        assertEquals("SELECT SUM(t.amount) AS totalAmount, c.name AS customerName FROM transaction t INNER JOIN customer c ON c.ID=t.CUST_ID_FK GROUP BY c.name",
                new Query(transaction).select(totalAmount).groupBy(transactionCustomer.apply(customerName)).print());
    }
}
