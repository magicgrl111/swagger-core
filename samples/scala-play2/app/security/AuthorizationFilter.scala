package security

import com.wordnik.swagger.play.ApiAuthorizationFilter
import play.Logger
import java.io.File
import java.util.ArrayList
import java.net.URLDecoder

import play.api.mvc.RequestHeader 

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

class AuthorizationFilter extends ApiAuthorizationFilter {
  val resourceAccess = Map(
    "/user.{format}" -> false,
    "/pet.{format}" -> false,
    "/store.{format}" -> true)

  val operationAccess = Map(
    "GET:/pet.{format}/{petId}" -> false,
    "POST:/pet.{format}" -> true,
    "PUT:/pet.{format}" -> true,
    "GET:/pet.{format}/findByStatus" -> false,
    "GET:/pet.{format}/findByTags" -> false,

    "GET:/store.{format}/order" -> true,
    "GET:/store.{format}/order/{orderId}" -> true,
    "DELETE:/store.{format}/order/{orderId}" -> true,
    "POST:/store.{format}/order" -> true,

    "POST:/user.{format}" -> true,
    "POST:/user.{format}/createWithArray" -> true,
    "POST:/user.{format}/createWithList" -> true,
    "PUT:/user.{format}/{username}" -> true,
    "DELETE:/user.{format}/{username}" -> true,
    "GET:/user.{format}/{username}" -> false,
    "GET:/user.{format}/login" -> false,
    "GET:/user.{format}/logout" -> false)

  var securekeyId = "special-key"
  var unsecurekeyId = "default-key"

  def authorize(apiPath: String, httpMethod: String)(implicit requestHeader: RequestHeader): Boolean = {
  	val isAuthorized = if(requestHeader != null) {
      isPathSecure(httpMethod.toUpperCase + ":" + apiPath, false) match {
        case true => apiKey == securekeyId
        case false => true
      }
  	} else {
  		Logger.debug("no header to authorize path " + apiPath)
  		false
  	}

    Logger.debug("authorizing path " + httpMethod + ":" + apiPath + ", " + isAuthorized)

    isAuthorized
  }

  def authorizeResource(resourcePath: String)(implicit requestHeader: RequestHeader): Boolean = {
    Logger.debug("authorizing resource " + resourcePath)
    isPathSecure(resourcePath, true) match {
      case true => apiKey.equals(securekeyId)
      case false => true
    }
  }

  private def apiKey()(implicit requestHeader: RequestHeader): String = {
    if(requestHeader == null) null
  	else requestHeader.queryString.get("api_key") match {
  		case Some(keySeq) => keySeq.head
  		case None => null
  	}
  }

  private def isPathSecure(apiPath: String, isResource: Boolean): Boolean = {
    isResource match {
      case true => resourceAccess.getOrElse(apiPath, false)
      case false => operationAccess.getOrElse(apiPath, false)
    }
  }
}
