/*
 * Copyright(c) 2019 NTT Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.example.m9amsa.account.spy;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.m9amsa.account.entity.LoginStatus;
import com.example.m9amsa.account.entity.LoginStatusRepository;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor()
public class LoginStatusRepositorySpy implements LoginStatusRepository {
    private LoginStatusRepository repository;

    public <S extends LoginStatus> S save(S entity) {
        return repository.save(entity);
    }

    public <S extends LoginStatus> Optional<S> findOne(Example<S> example) {
        return repository.findOne(example);
    }

    public Page<LoginStatus> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public List<LoginStatus> findAll() {
        return repository.findAll();
    }

    public List<LoginStatus> findAll(Sort sort) {
        return repository.findAll(sort);
    }

    public Optional<LoginStatus> findById(Long id) {
        return repository.findById(id);
    }

    public List<LoginStatus> findAllById(Iterable<Long> ids) {
        return repository.findAllById(ids);
    }

    public <S extends LoginStatus> List<S> saveAll(Iterable<S> entities) {
        return repository.saveAll(entities);
    }

    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    public void flush() {
        repository.flush();
    }

    public <S extends LoginStatus> S saveAndFlush(S entity) {
        return repository.saveAndFlush(entity);
    }

    public void deleteInBatch(Iterable<LoginStatus> entities) {
        repository.deleteInBatch(entities);
    }

    public <S extends LoginStatus> Page<S> findAll(Example<S> example, Pageable pageable) {
        return repository.findAll(example, pageable);
    }

    public long count() {
        return repository.count();
    }

    public void deleteAllInBatch() {
        repository.deleteAllInBatch();
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public LoginStatus getOne(Long id) {
        return repository.getOne(id);
    }

    public void delete(LoginStatus entity) {
        repository.delete(entity);
    }

    public <S extends LoginStatus> long count(Example<S> example) {
        return repository.count(example);
    }

    public void deleteAll(Iterable<? extends LoginStatus> entities) {
        repository.deleteAll(entities);
    }

    public <S extends LoginStatus> boolean exists(Example<S> example) {
        return repository.exists(example);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public <S extends LoginStatus> List<S> findAll(Example<S> example) {
        return repository.findAll(example);
    }

    public <S extends LoginStatus> List<S> findAll(Example<S> example, Sort sort) {
        return repository.findAll(example, sort);
    }
}
