package com.codebranch.scala.mongodb

import com.mongodb.{BasicDBObject, WriteResult, DBObject}
import org.bson.types.ObjectId
import com.codebranch.scala.mongodb.handlers._

class CollectionObject[T <: Entity with EntityId](implicit manifest: Manifest[T], th: TypeHandler[T]) {

  private def collection(implicit mongo: MongoClient) =
    mongo.getCollection[T]

  def drop()(implicit mongo: MongoClient) {
    collection.drop()
  }

  def find(query: DBObject, fields: DBObject = null)(implicit mongo: MongoClient): Cursor[T] =
    collection.find[T](query, fields)

  def findRaw(query: DBObject, fields: DBObject = null)(implicit mongo: MongoClient): RawCursor =
    collection.findRaw(query, fields)

  def findOne(query: DBObject, fields: DBObject = null)(implicit mongo: MongoClient): Option[T] =
    collection.findOne[T](query, fields, null)

  def findOneRaw(query: DBObject, fields: DBObject = null)(implicit mongo: MongoClient): Option[DBObject] =
    collection.findOneRaw(query, fields, null)

  def findById(id: ObjectId, fields: DBObject = null)(implicit mongo: MongoClient): Option[T] =
    findOne(DBObjectGen.compose(EntityId.Field.Id, id), fields)

  def findByIdRaw(id: ObjectId, fields: DBObject = null)(implicit mongo: MongoClient): Option[DBObject] =
    findOneRaw(DBObjectGen.compose(EntityId.Field.Id, id), fields)

  def save(entity: T)(implicit mongo: MongoClient): WriteResult = collection.save(entity)

  def saveRaw(obj: DBObject)(implicit mongo: MongoClient): WriteResult = collection.save(obj)

  def insert(entity: T)(implicit mongo: MongoClient): WriteResult = collection.insert(entity)

  def update(query: DBObject, obj: DBObject, upsert: Boolean = false, multi: Boolean = false)(implicit mongo: MongoClient): WriteResult = {
    Logger.debug(s"update($query, $obj, multi = $multi, upsert = $upsert)")
    collection.update(query, obj, upsert, multi)
  }

  def remove(query: DBObject)(implicit mongo: MongoClient): WriteResult =
    collection.remove(query)

  def remove(entity: T)(implicit mongo: MongoClient): WriteResult =
    entity match {
      case entityId: EntityId =>
        collection.remove(new BasicDBObject(EntityId.Field.Id, entityId.id.get))
      case _ =>
        collection.remove(entity)
    }

  def aggregate(first: DBObject, others: DBObject*)(implicit mongo: MongoClient) = {
    Logger.debug(s"aggregate($first, $others)")
    collection.aggregate(first, others:_*)
  }

  def ensureIndex(keys: Map[String, Int], name: Option[String] = None, unique: Boolean = false)(implicit mongo: MongoClient): Unit = {
    collection.ensureIndex(keys, name, unique)
  }

  def ensureIndex(key: String)(implicit mongo : MongoClient): Unit = {
    collection.ensureIndex(key)
  }

  def count(implicit mongo: MongoClient) = collection.count
}
