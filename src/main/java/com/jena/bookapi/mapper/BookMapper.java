package com.jena.bookapi.mapper;

import com.jena.bookapi.dto.BookRequest;
import com.jena.bookapi.dto.BookResponse;
import com.jena.bookapi.entity.Book;
import org.mapstruct.*;

/**
 * MapStruct Mapper for Book Entity/DTO conversion
 *
 * <p>Interview Points: 1. MapStruct generates implementation at compile-time (no reflection
 * overhead) 2. @Mapper(componentModel = "spring") creates Spring bean 3. @Mapping handles field
 * name differences and custom conversions 4. Generated code is type-safe and performant
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookMapper {

    /**
     * Convert BookRequest to Book entity Interview Point: @Mapping ignores fields that should be set
     * by JPA/system
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Book toEntity(BookRequest request);

    /**
     * Convert Book entity to BookResponse Interview Point: All fields are mapped automatically due to
     * matching names
     */
    BookResponse toResponse(Book book);

    /**
     * Update existing Book entity from BookRequest Interview Point: @MappingTarget updates existing
     * object instead of creating new one
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Book book, BookRequest request);
}
