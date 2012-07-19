package com.github.teaplanet.http

import org.specs2.mutable._
import xml.Elem

/**
 * Http client spec.
 */
class HttpSpec extends Specification {

	val yahoo_com = "http://www.yahoo.com/"
	val google_co_jp = "http://www.google.co.jp/"
	val https_google_co_jp = "https://www.google.co.jp/"
	val google_com = "http://www.google.com/"

	"HTTP GET" should {
		"access http" in {
			Http().get(yahoo_com).statusCode must_== 200
		}

		"access https" in {
			Http().get(https_google_co_jp).statusCode must_== 200
		}

		"http redirect to https(http://google.com/ -> https://google.co.jp/)" in {
			Http().get(google_com).statusCode must_== 200
		}
	}

	"HTTP(object) GET" should {
		"access http" in {
			Http.get(yahoo_com).statusCode must_== 200
		}

		"access https" in {
			Http.get(https_google_co_jp).statusCode must_== 200
		}

		"http redirect to https(http://google.com/ -> https://google.co.jp/)" in {
			Http.get(google_com).statusCode must_== 200
		}
	}

	"confirm User-Agent" should {
		"chrome" in {
			val http = Http().userAgent(UserAgent.CHROME_20)
			val res = http.get("http://www.ugtop.com/spill.shtml")
			res.statusCode must_== 200

			val body = res.bodyAsString
			body must beSome[String].which(_.contains(UserAgent.CHROME_20))
		}
	}

	"HTTP response" should {
		"response is xml" in {
			val tenkiTokyo = "http://feed.tenki.jp/component/static_api/rss/warn/pref_16.xml"
			val res = Http.get(tenkiTokyo)
			res.statusCode must_== 200
			res.bodyAsXML must beSome[Elem]
		}
	}

}
