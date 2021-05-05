package com.santos.repository;

import org.springframework.data.repository.CrudRepository;

import com.santos.model.Shortcut;
import org.springframework.stereotype.Repository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete
@Repository
public interface ShortcutRepository extends CrudRepository<Shortcut, Integer> {
}