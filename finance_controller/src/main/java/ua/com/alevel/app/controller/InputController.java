package ua.com.alevel.app.controller;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ua.com.alevel.app.service.AddingOperation;
import ua.com.alevel.app.service.WritingToCsv;

import javax.persistence.EntityManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class InputController {

    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public InputController(List<String> config) {

        Configuration configuration = new Configuration().configure();
        configuration.setProperty("hibernate.connection.username", config.get(0));
        configuration.setProperty("hibernate.connection.password", config.get(1));

        try (SessionFactory sessionFactory = configuration.buildSessionFactory()) {
            EntityManager entityManager = sessionFactory.createEntityManager();

            while (true) {
                System.out.println("Choose an action:\n" +
                        "1 -> add new operation\n" +
                        "2 -> export operations of account in certain period\n" +
                        "3 -> exit");

                switch (reader.readLine()) {
                    case "1":
                        AddingOperation addingOperation = new AddingOperation();
                        addingOperation.addOperation(entityManager, Integer.valueOf(config.get(2)));
                        break;
                    case "2":
                        WritingToCsv writingToCsv = new WritingToCsv();
                        writingToCsv.exportInCSV(config);
                        break;
                    case "0":
                        entityManager.close();
                        System.exit(0);
                    default:
                        System.out.println("Wrong input");
                }
            }
        } catch (IOException e) {
            System.err.println("Something went wrong, please try again");
        }
    }
}
