package com.github.teaplanet.http

import org.apache.http.impl.client.{DefaultRedirectStrategy, DefaultHttpClient}
import org.apache.http.client.methods.HttpGet
import org.apache.http.{NameValuePair, HttpVersion, HttpResponse}
import org.apache.http.params.{CoreProtocolPNames, HttpProtocolParams}
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair
import org.apache.http.impl.conn.{PoolingClientConnectionManager, ProxySelectorRoutePlanner}
import org.apache.http.client.params.ClientPNames
import java.net.ProxySelector
import java.nio.charset.{CodingErrorAction, Charset}
import io.Source
import scala.collection.JavaConversions._
import xml.{XML, Elem}
import java.io.InputStream

/**
 * Http client.
 */

object Http {

	val UTF_8 = "UTF-8"

	val VERSION_1_1 = HttpVersion.HTTP_1_1
	val VERSION_1_0 = HttpVersion.HTTP_1_0
	val VERSION_0_9 = HttpVersion.HTTP_0_9

	def get(url:String, params:Iterable[(String, String)]=Map(), headers:Iterable[(String, String)]=Map())(implicit process:Response => Unit = null):Response = {
		Http().get(url, params, headers)(process)
	}

}

case class Http() {

	val client = new DefaultHttpClient(new PoolingClientConnectionManager)
	private val httpParams = client.getParams

	client.setRoutePlanner(new ProxySelectorRoutePlanner(
		client.getConnectionManager.getSchemeRegistry, ProxySelector.getDefault))

	client.setRedirectStrategy(new DefaultRedirectStrategy)

	def redirect(handle:Boolean=true):Http = {
		httpParams.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, handle)
		this
	}

	def version(ver:HttpVersion=Http.VERSION_1_1):Http = {
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1)
		this
	}

	def userAgent(name:String=UserAgent.HTTP_COMPONENTS):Http = {
		httpParams.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, true)
		HttpProtocolParams.setUserAgent(httpParams, name)
		this
	}

	private def mkQueryString(params:Iterable[(String, String)]):List[NameValuePair] = {
		params.foldLeft(List[NameValuePair]()) { case (pair, (name, value)) =>
			pair :+ new BasicNameValuePair(name, value)
			pair
		}
	}

	def get(url:String, params:Iterable[(String, String)]=Map(), headers:Iterable[(String, String)]=Map(), charset:String=Http.UTF_8)(implicit process:Response => Unit = null):Response = {
		val urlQuery = params.isEmpty match {
			case true => url
			case false =>
				val query:String = URLEncodedUtils.format(mkQueryString(params), charset)
				val sep = url.contains("?") match {
					case true => "&"
					case false => "?"
				}
				String.format("%s%s%s", url, sep, query)
		}
		val request = new HttpGet(urlQuery)
		headers.foreach { header =>
			request.addHeader(header._1, header._2)
		}
		val response = client.execute(request)
		val res = Response(response)
		if (process != null) process(res)
		res
	}

	def close() { client.getConnectionManager.shutdown() }

}

case class Response(httpResponse:HttpResponse) {

	def statusCode:Int = httpResponse.getStatusLine.getStatusCode

	def body:InputStream = httpResponse.getEntity.getContent

	def bodyAsString:Option[String] = httpResponse.getEntity match {
		case null => None
		case entity =>
			val decorder = Charset.forName(Http.UTF_8).newDecoder()
			decorder.onMalformedInput(CodingErrorAction.IGNORE)
			Some(Source.fromInputStream(entity.getContent)(decorder).getLines().mkString("\n"))
	}

	def bodyAsXML:Option[Elem] = httpResponse.getEntity match {
		case null => None
		case entity => Some(XML.load(entity.getContent))
	}

	def headers:Map[String, String] = (httpResponse.getAllHeaders.map { header =>
		header.getName -> header.getValue
	}).toMap

	def header(name:String):Array[String] = httpResponse.getHeaders(name).map { header =>
		header.getValue
	}

}

object UserAgent {
	val HTTP_COMPONENTS = "HttpComponents"
	val CHROME_20 = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_4) AppleWebKit/536.11 (KHTML, like Gecko) Chrome/20.0.1132.47 Safari/536.11"
}
