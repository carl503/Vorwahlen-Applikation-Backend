package ch.zhaw.vorwahlen.mapper;

public interface Mapper<T, S> {
    T toDto(S param);
}