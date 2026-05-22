package cpe223.group8.eggspress.repository;

import java.util.List;

public interface BaseRepository<T> {
    // [todo]
    void save(T entity);
    T findById(int id);
    List<T> findAll();
    void update(T entity);
    void delete(int id);
}
