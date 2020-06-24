package com.seongje.studyolle.modules.tag.service;

import com.seongje.studyolle.modules.tag.domain.Tag;
import com.seongje.studyolle.modules.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public Tag findOrCreateTag(String tagTitle) {
        Tag tag = tagRepository.findByTitle(tagTitle);

        if (tag == null) {
            tag = tagRepository.save(Tag.builder().title(tagTitle).build());
        }

        return tag;
    }

    public Tag findByTagTitle(String tagTitle) {
        return tagRepository.findByTitle(tagTitle);
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll().stream()
                .sorted(comparing(Tag::getTitle))
                .collect(toList());
    }
}
