// package test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.Assert.assertFalse;

import java.util.Date;
import java.util.List;

// import javax.swing.JButton;
// import javax.swing.JFormattedTextField;
// import javax.swing.JOptionPane;
// import javax.swing.SwingUtilities;

// import java.beans.Transient;
// import java.text.NumberFormat;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import controller.ExpenseTrackerController;
import model.ExpenseTrackerModel;
import model.Transaction;
import model.Filter.AmountFilter;
import model.Filter.CategoryFilter;
import view.ExpenseTrackerView;

import java.awt.*;
import model.Filter.AmountFilter;
import model.Filter.CategoryFilter;



public class TestExample {
  
  private ExpenseTrackerModel model;
  private ExpenseTrackerView view;
  private ExpenseTrackerController controller;

  @Before
  public void setup() {
    model = new ExpenseTrackerModel();
    view = new ExpenseTrackerView();
    controller = new ExpenseTrackerController(model, view);
  }

    public double getTotalCost() {
        double totalCost = 0.0;
        List<Transaction> allTransactions = model.getTransactions(); // Using the model's getTransactions method
        for (Transaction transaction : allTransactions) {
            totalCost += transaction.getAmount();
        }
        return totalCost;
    }


    public void checkTransaction(double amount, String category, Transaction transaction) {
	assertEquals(amount, transaction.getAmount(), 0.01);
        assertEquals(category, transaction.getCategory());
        String transactionDateString = transaction.getTimestamp();
        Date transactionDate = null;
        try {
            transactionDate = Transaction.dateFormatter.parse(transactionDateString);
        }
        catch (ParseException pe) {
            pe.printStackTrace();
            transactionDate = null;
        }
        Date nowDate = new Date();
        assertNotNull(transactionDate);
        assertNotNull(nowDate);
        // They may differ by 60 ms
        assertTrue(nowDate.getTime() - transactionDate.getTime() < 60000);
    }


    @Test
    public void testAddTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add a transaction
	double amount = 50.0;
	String category = "food";
        assertTrue(controller.addTransaction(amount, category));
    
        // Post-condition: List of transactions contains only
	//                 the added transaction	
        assertEquals(1, model.getTransactions().size());
    
        // Check the contents of the list
	Transaction firstTransaction = model.getTransactions().get(0);
	checkTransaction(amount, category, firstTransaction);
	
	// Check the total amount
        assertEquals(amount, getTotalCost(), 0.01);
    }


    @Test
    public void testRemoveTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add and remove a transaction
	double amount = 50.0;
	String category = "food";
        Transaction addedTransaction = new Transaction(amount, category);
        model.addTransaction(addedTransaction);
    
        // Pre-condition: List of transactions contains only
	//                the added transaction
        assertEquals(1, model.getTransactions().size());
	Transaction firstTransaction = model.getTransactions().get(0);
	checkTransaction(amount, category, firstTransaction);

	assertEquals(amount, getTotalCost(), 0.01);
	
	// Perform the action: Remove the transaction
        model.removeTransaction(addedTransaction);
    
        // Post-condition: List of transactions is empty
        List<Transaction> transactions = model.getTransactions();
        assertEquals(0, transactions.size());
    
        // Check the total cost after removing the transaction
        double totalCost = getTotalCost();
        assertEquals(0.00, totalCost, 0.01);
    }

    @Test
    public void testAddTransactionView(){
        //checking pre-conditions: number of transactions and total cost
        assertEquals(0, model.getTransactions().size());
        assertEquals(0, getTotalCost(), 0.01);

        //adding transaction using controller
        double amount = 50.0;
	    String category = "food";
        Transaction dt = new Transaction(amount, category);
        assertTrue(controller.addTransaction(amount, category));

        //verifying that the transaction is added using the model
        assertEquals(1, model.getTransactions().size());

        //checking postconditions: verifying that the transaction is added using the view
        List<Transaction> transactionsFromTable = view.getAllTransactionsFromTable();
        assertEquals("Amount from view is not equal to the amount added",dt.getAmount(), transactionsFromTable.get(0).getAmount(), 0.01);
        assertEquals("Category from view is not equal to the category added", dt.getCategory(), transactionsFromTable.get(0).getCategory());
        assertEquals("Timestamp from view is not equal to the timestamp added", dt.getTimestamp(), transactionsFromTable.get(0).getTimestamp());

        //checking postconditions: Testing total cost using the view
        assertEquals("total cost from the view is not equal to the total cost in the model", view.getTotalCostFromTable(), getTotalCost(), 0.01);

    }

     @Test
    public void testTransactionWithInvalidAmount() {
        // Pre-condition: List of transactions is empty and total cost is 0 initially
        assertEquals(0, model.getTransactions().size());
        assertEquals(0, getTotalCost(), 0.01);

        double invalidAmt = -50.0; 
        String validCategory = "Food";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Transaction(invalidAmt, validCategory);
        });
        
        // Post-condition: Check that no transactions were added and that the exception message returned is as expected
        assertEquals("The amount is not valid.", thrown.getMessage());
        assertEquals("The transactions list should be empty after the invalid operation", 0, model.getTransactions().size());
        assertEquals("The total cost shall remain unchanged after the invalid operation", 0, getTotalCost(), 0.01);

        // Pre-condition: List of transactions is empty and total cost is 0 initially
        assertEquals(0, model.getTransactions().size());
        assertEquals(0, getTotalCost(), 0.01);

        Transaction nullTransaction = null;

        IllegalArgumentException thrown_null = assertThrows(IllegalArgumentException.class, () -> {
        model.addTransaction(nullTransaction);
        });

        // Check that no transactions were added
        assertEquals("The new transaction must be non-null.", thrown_null.getMessage());
        assertEquals("Transactions list should be empty after invalid operation", 0, model.getTransactions().size());
        assertEquals("Total cost should remain unchanged after invalid operation", 0, getTotalCost(), 0.01);

        }



    @Test
    public void testAmountFilter() {
         // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Add transactions to the model
        double amountToFilter = 100.0;
        model.addTransaction(new Transaction(50.0, "food"));
        model.addTransaction(new Transaction(amountToFilter, "food"));
        model.addTransaction(new Transaction(200.0, "travel"));
        model.addTransaction(new Transaction(amountToFilter, "food"));

        // Create an AmountFilter for the amount = amountToFilter
        AmountFilter filter = new AmountFilter(100.0);

        // Apply the filter 
        List<Transaction> filteredTransactions = filter.filter(model.getTransactions());

        //Post conditions: Check that the filtered list only contains transactions with the amount = amountToFilter
        assertNotNull("Filtered transactions list shouldn't be null", filteredTransactions);
        assertEquals("There should be two transactions with the amount= 100", 2, filteredTransactions.size());
        for (Transaction transac : filteredTransactions) {
            assertEquals("The filtered transaction should have amount 100.0", amountToFilter, transac.getAmount(), 0.0);
        }
    }

    @Test
    public void testCategoryFilter() {
         // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Add transactions with different categories directly to the model
        model.addTransaction(new Transaction(50.0, "Food"));
        model.addTransaction(new Transaction(100.0, "Travel"));
        model.addTransaction(new Transaction(50.0, "Travel"));
        model.addTransaction(new Transaction(150.0, "Food"));

        // Create a CategoryFilter 
        CategoryFilter filter = new CategoryFilter("Travel");

        // Apply the filter 
        List<Transaction> filteredTransactions = filter.filter(model.getTransactions());

        // Post-condition: Check that the filtered list only contains transactions with the category "Travel"
        assertNotNull("The filtered transactions list should not be null", filteredTransactions);
        assertEquals("There should be only two transactions with the category= Travel", 2, filteredTransactions.size());
        for (Transaction transac : filteredTransactions) {
            assertTrue("Filtered transaction should have category Travel",
                       transac.getCategory().equalsIgnoreCase("Travel"));
        }
    }



}
