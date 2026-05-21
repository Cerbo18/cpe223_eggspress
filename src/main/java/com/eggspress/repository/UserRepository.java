package com.eggspress.repository;

import com.eggspress.models.User;
import java.util.ArrayList;
import java.util.List;

public class UserRepository implements BaseRepository<User> {
    private static final List<User> users = new ArrayList<>();

    static {
        // Balhin ang acc details dri
        users.add(new User("admin", "123"));
    }

    public static List<User> getStaticUsers() {
        return users;
    }

    public static void addStaticUser(User user) {
        users.add(user);
    }

    @Override
    public void save(User entity) {
        users.add(entity);
    }

    @Override
    public User findById(int id) {
        // [todo]
        return null;
    }

    @Override
    public List<User> findAll() {
        return users;
    }

    @Override
    public void update(User entity) {
        // [todo]
    }

    @Override
    public void delete(int id) {
        // [todo]
    }
}
