package com.github.teaplanet.http

import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.{NameValuePair, HttpVersion, HttpResponse}
import org.apache.http.params.{BasicHttpParams, HttpProtocolParams}
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.apache.http.client.params.ClientPNames
import java.net.ProxySelector
import io.Source
import scala.collection.JavaConversions._

/**
 * Http client.
 *
 * Author: Ken
 * Date: 2012-07-05 0:36
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

	val client = new DefaultHttpClient
	val params = new BasicHttpParams

	this.init()

	private def init():Unit = {
		client.setRoutePlanner(new ProxySelectorRoutePlanner(
			client.getConnectionManager.getSchemeRegistry, ProxySelector.getDefault))
		redirect()
		version()
	}

	def redirect(enabled:Boolean=true):Http = {
		params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, enabled)
		this
	}

	def version(ver:HttpVersion=Http.VERSION_1_1):Http = {
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1)
		this
	}

	def userAgent(name:String=UserAgent.HTTP_COMPONENTS):Http = {
		HttpProtocolParams.setUserAgent(params, name)
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

	def close():Unit = client.getConnectionManager.shutdown()

}

case class Response(httpResponse:HttpResponse) {

	def statusCode:Int = httpResponse.getStatusLine.getStatusCode

	def bodyAsString:Option[String] = httpResponse.getEntity match {
		case null => None
		case entity => Some(Source.fromInputStream(entity.getContent).getLines().mkString)
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
