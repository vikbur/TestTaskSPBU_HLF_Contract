package com.owlike.genson;

import java.io.IOException;

import com.owlike.genson.stream.ObjectReader;

/**
 * Deserializers handle deserialization by reading data form {@link com.owlike.genson.stream.ObjectReader
 * ObjectReader} and constructing java objects of type T. Genson Deserializers work like classic
 * deserializers from other libraries.
 *
 * @param <T> the type of objects this deserializer can deserialize.
 * @author eugen
 * @see Converter
 */
public interface Deserializer<T> {
  /**
   * @param reader used to read data from.
   * @param ctx    the current context.
   * @return an instance of T or a subclass of T.
   * @throws com.owlike.genson.JsonBindingException
   * @throws com.owlike.genson.stream.JsonStreamException
   */
  public T deserialize(ObjectReader reader, Context ctx) throws Exception;
}
