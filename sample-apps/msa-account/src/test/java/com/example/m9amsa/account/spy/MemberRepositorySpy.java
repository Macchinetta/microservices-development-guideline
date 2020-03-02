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

import com.example.m9amsa.account.entity.Member;
import com.example.m9amsa.account.entity.MemberRepository;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberRepositorySpy implements MemberRepository {

    private MemberRepository org;

    public <S extends Member> S save(S entity) {
        return org.save(entity);
    }

    public <S extends Member> Optional<S> findOne(Example<S> example) {
        return org.findOne(example);
    }

    public Page<Member> findAll(Pageable pageable) {
        return org.findAll(pageable);
    }

    public List<Member> findAll() {
        return org.findAll();
    }

    public List<Member> findAll(Sort sort) {
        return org.findAll(sort);
    }

    public Optional<Member> findById(Long id) {
        return org.findById(id);
    }

    public List<Member> findAllById(Iterable<Long> ids) {
        return org.findAllById(ids);
    }

    public <S extends Member> List<S> saveAll(Iterable<S> entities) {
        return org.saveAll(entities);
    }

    public boolean existsById(Long id) {
        return org.existsById(id);
    }

    public void flush() {
        org.flush();
    }

    public <S extends Member> S saveAndFlush(S entity) {
        return org.saveAndFlush(entity);
    }

    public void deleteInBatch(Iterable<Member> entities) {
        org.deleteInBatch(entities);
    }

    public <S extends Member> Page<S> findAll(Example<S> example, Pageable pageable) {
        return org.findAll(example, pageable);
    }

    public long count() {
        return org.count();
    }

    public void deleteAllInBatch() {
        org.deleteAllInBatch();
    }

    public void deleteById(Long id) {
        org.deleteById(id);
    }

    public Member getOne(Long id) {
        return org.getOne(id);
    }

    public void delete(Member entity) {
        org.delete(entity);
    }

    public <S extends Member> long count(Example<S> example) {
        return org.count(example);
    }

    public void deleteAll(Iterable<? extends Member> entities) {
        org.deleteAll(entities);
    }

    public <S extends Member> boolean exists(Example<S> example) {
        return org.exists(example);
    }

    public void deleteAll() {
        org.deleteAll();
    }

    public <S extends Member> List<S> findAll(Example<S> example) {
        return org.findAll(example);
    }

    public <S extends Member> List<S> findAll(Example<S> example, Sort sort) {
        return org.findAll(example, sort);
    }
}
