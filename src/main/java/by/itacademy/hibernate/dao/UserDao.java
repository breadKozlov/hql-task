package by.itacademy.hibernate.dao;


import by.itacademy.hibernate.entity.Payment;
import by.itacademy.hibernate.entity.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.Session;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDao {

    private static final UserDao INSTANCE = new UserDao();

    /**
     * Возвращает всех сотрудников
     */
    public List<User> findAll(Session session) {
        return session.createQuery("FROM User",User.class).list();
    }

    /**
     * Возвращает всех сотрудников с указанным именем
     */
    public List<User> findAllByFirstName(Session session, String firstName) {

        return session.createQuery("FROM User U WHERE U.personalInfo.firstname = :firstName",User.class)
                .setParameter("firstName",firstName).list();
    }

    /**
     * Возвращает первые {limit} сотрудников, упорядоченных по дате рождения (в порядке возрастания)
     */
    public List<User> findLimitedUsersOrderedByBirthday(Session session, int limit) {
        return session.createQuery("FROM User U ORDER BY U.personalInfo.birthDate asc",User.class)
                .setMaxResults(limit).list();
    }

    /**
     * Возвращает всех сотрудников компании с указанным названием
     */
    public List<User> findAllByCompanyName(Session session, String companyName) {

        return session.createQuery("FROM User U WHERE U.company.name = :name",User.class)
                .setParameter("name",companyName).list();
    }

    /**
     * Возвращает все выплаты, полученные сотрудниками компании с указанными именем,
     * упорядоченные по имени сотрудника, а затем по размеру выплаты
     */
    public List<Payment> findAllPaymentsByCompanyName(Session session, String companyName) {
        return session.createQuery("FROM Payment P WHERE P.receiver.company.name = :name" +
                " ORDER BY P.receiver.personalInfo.firstname asc, P.amount asc",Payment.class)
                .setParameter("name",companyName).list();
    }

    /**
     * Возвращает среднюю зарплату сотрудника с указанными именем и фамилией
     */
    public Double findAveragePaymentAmountByFirstAndLastNames(Session session, String firstName, String lastName) {
        var result = session.createQuery("SELECT P.receiver.personalInfo.firstname as firstname," +
                " P.receiver.personalInfo.lastname as lastname, avg(P.amount) FROM Payment P " +
                "WHERE firstname = :firstname AND lastname = :lastname" +
                " GROUP BY firstname,lastname",Object[].class)
                .setParameter("firstname",firstName)
                .setParameter("lastname",lastName).list().stream().findFirst().orElseThrow();
        return (Double) result[2];
    }

    /**
     * Возвращает для каждой компании: название, среднюю зарплату всех её сотрудников. Компании упорядочены по названию.
     */
    public List<Object[]> findCompanyNamesWithAvgUserPaymentsOrderedByCompanyName(Session session) {
        return session.createQuery("select rec.company.name as name,avg (P.amount)" +
                " from Payment P join P.receiver as rec join rec.company group by name order by name",Object[].class).list();
    }

    /**
     * Возвращает список: сотрудник (объект User), средний размер выплат, но только для тех сотрудников, чей средний размер выплат
     * больше среднего размера выплат всех сотрудников
     * Упорядочить по имени сотрудника
     */
    public List<Object[]> isItPossible(Session session) {

        var allAvg = session.createQuery("select avg(P.amount) from Payment P").getSingleResult();
        return session.createQuery("select user,avg(P.amount) from Payment P join P.receiver as user " +
                        "group by user.id having avg(P.amount) > :allAvg order by P.receiver.personalInfo.firstname asc",Object[].class)
                .setParameter("allAvg",allAvg).list();
    }

    public static UserDao getInstance() {
        return INSTANCE;
    }
}