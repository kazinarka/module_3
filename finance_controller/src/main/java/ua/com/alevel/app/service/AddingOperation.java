package ua.com.alevel.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.com.alevel.app.model.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;

public class AddingOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger("logs");

    public void addOperation(EntityManager entityManager, Integer userId) {
        try {
            entityManager.getTransaction().begin();
            Query query = entityManager.createQuery("select user from User user where id = :userId");
            query.setParameter("userId", userId);
            query.setMaxResults(1);
            User user = (User) query.getSingleResult();

            if (user == null) {
                LOGGER.error("No such user. Wrong id: " + userId);
                throw new RuntimeException("No such user. Input was wrong");
            } else {
                System.out.println("User signed in");
                LOGGER.info("User signed in. Id: " + userId);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            Operation operation = new Operation();
            System.out.println("Choose account:");
            List<Account> accounts = user.getAccount();

            for (Account account : accounts) {
                System.out.println("id:" + account.getId() + ", balance:" + account.getAmount());
            }

            String index = reader.readLine();
            LOGGER.info("Index of account: " + index);
            Account account = accounts.get(Integer.parseInt(index) - 1);

            System.out.println("Choose the category of operation:\n" +
                    "1 -> income\n" +
                    "2 -> consumption");

            switch (reader.readLine()) {
                case "1":
                    System.out.println("Input value of income:");
                    long value = Long.parseLong(reader.readLine());
                    LOGGER.info("Inputted value: " + value);
                    System.out.println("Input description of income:");
                    String description = reader.readLine();
                    LOGGER.info("Inputted description: " + description);

                    Income income = new Income();
                    income.setDescription(description);
                    income.setCategories(Category.Categories.INCOME);
                    entityManager.persist(income);
                    operation.setValue(value);
                    operation.setTime(Instant.now());
                    operation.setCategory(income);
                    operation.setAccount(account);
                    entityManager.persist(operation);
                case "2":
                    System.out.println("Input value of consumption:");
                    value = Long.parseLong(reader.readLine());
                    LOGGER.info("Inputted value: " + value);
                    System.out.println("Input description of consumption:");
                    description = reader.readLine();
                    LOGGER.info("Inputted description: " + description);

                    Consumption consumption = new Consumption();
                    consumption.setDescription(description);
                    consumption.setCategories(Category.Categories.CONSUMPTION);
                    entityManager.persist(consumption);
                    operation.setValue(value);
                    operation.setTime(Instant.now());
                    operation.setCategory(consumption);
                    operation.setAccount(account);
                    entityManager.persist(operation);
                default:
                    LOGGER.warn("Incorrect input while choosing category");
                    System.out.println("Wrong input");
            }
            entityManager.getTransaction().commit();
            System.out.println("Operation is successfully added. Current amount of the account: " + account.getAmount());
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }
}