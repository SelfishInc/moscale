package com.codebranch.scala.mongodb

import com.{mongodb => jmdb}
import jmdb.{WriteResult, DBObject, AggregationOutput}
import scala.util.control.ControlThrowable
import handlers._

case class InvalidFields(errors: List[String]) extends ControlThrowable

class Collection(val jColl: jmdb.DBCollection) {
  val name = jColl.getName

  def count = jColl.count()

  def drop() {
    jColl.drop()
  }


  def find[T](implicit th : TypeHandler[T]) : Cursor[T] = find[T](null : DBObject, null : DBObject)


	def find[T](query: DBObject, fields : DBObject)(implicit th: TypeHandler[T]): Cursor[T] = {
    Logger.debug("Find. Query = %s" format query)
    new Cursor[T](jColl.find(query, fields))
  }


  def find[T](query : Value.Map, fields : Map[String, Boolean] = null)(implicit th: TypeHandler[T]) : Cursor[T] =
    find[T](toDBObject(query), toDBObject(fields))


  def findOne[T](query: DBObject, fields : DBObject, order : DBObject)(implicit th: TypeHandler[T]) : Option[T] = {
    Logger.debug("FindOne. Query = %s" format query.toString)
    val res = Option(jColl.findOne(query, fields, order)).map(th.fromDBObject(_))
    Logger.debug("Found %s" format res.toString)
    res
  }


	def findOne[T](query: Value.Map, fields : Map[String, Boolean] = null, order : Map[String, Int] = null)
      (implicit th: TypeHandler[T]): Option[T] =
    findOne[T](toDBObject(query), toDBObject(fields), toDBObject(order))


//  def findById[T](id : Value)(implicit th: TypeHandler[T]) : Option[T] =
//    findOne[T](Query(EntityId.Field.Id -> id))


  def save(dbo : DBObject) : WriteResult = jColl.save(dbo)

  def save(dbo : Value.Map) : WriteResult =
    save(toDBObject(dbo))

  def save[T <: Entity](entity : T)(implicit th: TypeHandler[T]): WriteResult =
	  entity match {
		  case e: EntityValidator =>
        val invalidFields = e.validate
			  if (invalidFields.isEmpty)
			    save(toDBObject(e))
        else
          throw new InvalidFields(invalidFields)
		  case e =>
			  save(toDBObject(e))
	  }

  def insert(dbo : DBObject) : WriteResult =
    jColl.insert(dbo)

  def insert(dbo : Value.Map) : WriteResult =
    insert(toDBObject(dbo))

  def insert[T <: Entity](entity : T)(implicit th: TypeHandler[T]) : WriteResult = {
	  entity match {
		  case e: EntityValidator =>
        val invalidFields = e.validate
        if (invalidFields.isEmpty)
          insert(toDBObject(e))
        else
          throw new InvalidFields(invalidFields)
		  case e =>
			  insert(toDBObject(e))
	  }
  }


  def update(query : DBObject, obj : DBObject, upsert : Boolean, multi : Boolean) : WriteResult =
    jColl.update(query, obj, upsert, multi)

  def update(query : Value.Map, obj : Value.Map, upsert : Boolean = false, multi : Boolean = false) : WriteResult =
    update(toDBObject(query), toDBObject(obj), upsert = upsert, multi = multi)


  def findAndModify(query: DBObject, update: DBObject, order: DBObject, fields: DBObject, upsert: Boolean, returnNew: Boolean): DBObject = {
    jColl.findAndModify(query, fields, order, false, update, returnNew, upsert)
  }


  def findAndModify[T]
  (query : Value.Map,
   update : Value.Map,
   order : Map[String, Int] = null,
   fields : Map[String, Boolean] = null,
   upsert : Boolean = false,
   returnNew : Boolean = false)
  (implicit th : TypeHandler[T])
  : Option[T] =
    Option(findAndModify(
      toDBObject(query),
      toDBObject(upsert),
      order = toDBObject(order),
      fields = toDBObject(fields),
      upsert = upsert,
      returnNew = returnNew)).map(th.fromDBObject(_))


  def remove(query : DBObject) : WriteResult =
    jColl.remove(query)


  def remove[T <: Entity](entity : T)(implicit th : TypeHandler[T]) : WriteResult =
    remove(toDBObject(entity))


  def aggregate(query: DBObject, additionalQueries: DBObject*): AggregationOutput = {
    Logger.debug(s"aggregate. Query:${query}")
    jColl.aggregate(query, additionalQueries:_*)
  }


  def ensureIndex(name : String) {
    jColl.ensureIndex(name)
  }

  def ensureIndex(keys : Map[String, Int], name : Option[String] = None, unique : Boolean = false) {
    jColl.ensureIndex(toDBObject(keys), name.orNull, unique)
  }

  def ensureIndex(keys : DBObject, options : DBObject) {
    jColl.ensureIndex(keys, options)
  }

  def ensureIndex(keys : Map[String, Int], options : Value.Map) {
    jColl.ensureIndex(toDBObject(keys), toDBObject(options))
  }


  private def toDBObject[T](obj : T)(implicit th : TypeHandler[T]) =
    Option(obj).map(Value(_).dbObject.asInstanceOf[DBObject]).orNull
}
