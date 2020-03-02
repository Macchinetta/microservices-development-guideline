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
package com.example.m9amsa.flightTicketFareCalculation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.m9amsa.flightTicketFareCalculation.constant.SeatClass;
import com.example.m9amsa.flightTicketFareCalculation.entity.SeatClassCharge;
import com.example.m9amsa.flightTicketFareCalculation.entity.SeatClassChargeRepository;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * JPA RepositoryのSpyクラス。
 * 
 * <pre>
 * MockitoでJPA RepositoryのSpyインスタンス生成ができないため、
 * 独自に実装。
 * </pre>
 * 
 */
@Data
@AllArgsConstructor()
public class SeatClassChargeRepositorySpy implements SeatClassChargeRepository {

    private SeatClassChargeRepository org;

    public <S extends SeatClassCharge> S save(S entity) {
        return org.save(entity);
    }

    public <S extends SeatClassCharge> Optional<S> findOne(Example<S> example) {
        return org.findOne(example);
    }

    public Page<SeatClassCharge> findAll(Pageable pageable) {
        return org.findAll(pageable);
    }

    public List<SeatClassCharge> findAll() {
        return org.findAll();
    }

    public List<SeatClassCharge> findAll(Sort sort) {
        return org.findAll(sort);
    }

    public Optional<SeatClassCharge> findById(SeatClass id) {
        return org.findById(id);
    }

    public List<SeatClassCharge> findAllById(Iterable<SeatClass> ids) {
        return org.findAllById(ids);
    }

    public <S extends SeatClassCharge> List<S> saveAll(Iterable<S> entities) {
        return org.saveAll(entities);
    }

    public boolean existsById(SeatClass id) {
        return org.existsById(id);
    }

    public void flush() {
        org.flush();
    }

    public <S extends SeatClassCharge> S saveAndFlush(S entity) {
        return org.saveAndFlush(entity);
    }

    public void deleteInBatch(Iterable<SeatClassCharge> entities) {
        org.deleteInBatch(entities);
    }

    public <S extends SeatClassCharge> Page<S> findAll(Example<S> example, Pageable pageable) {
        return org.findAll(example, pageable);
    }

    public long count() {
        return org.count();
    }

    public void deleteAllInBatch() {
        org.deleteAllInBatch();
    }

    public void deleteById(SeatClass id) {
        org.deleteById(id);
    }

    public SeatClassCharge getOne(SeatClass id) {
        return org.getOne(id);
    }

    public void delete(SeatClassCharge entity) {
        org.delete(entity);
    }

    public <S extends SeatClassCharge> long count(Example<S> example) {
        return org.count(example);
    }

    public void deleteAll(Iterable<? extends SeatClassCharge> entities) {
        org.deleteAll(entities);
    }

    public <S extends SeatClassCharge> boolean exists(Example<S> example) {
        return org.exists(example);
    }

    public void deleteAll() {
        org.deleteAll();
    }

    public <S extends SeatClassCharge> List<S> findAll(Example<S> example) {
        return org.findAll(example);
    }

    public <S extends SeatClassCharge> List<S> findAll(Example<S> example, Sort sort) {
        return org.findAll(example, sort);
    }

}
