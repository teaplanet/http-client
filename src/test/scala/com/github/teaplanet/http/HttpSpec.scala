package com.github.teaplanet.http

import org.specs2.mutable._

/**
 * Http client spec.
 *
 * Author: Ken
 * Date: 2012-07-05 0:45
 */
class HttpSpec extends Specification {

	val yahoo_com = "http://www.yahoo.com/"
	val google_co_jp = "http://www.google.co.jp/"
	val google_com = "http://www.google.com/"
	val hotmail_com = "http://hotmail.com/"

	"HTTP GET" should {
		"simple access to yahoo.com" in {
			Http().get(yahoo_com).statusCode must_== 200
		}

		"google.com redirect to google.co.jp" in {
			Http().get(google_co_jp).statusCode must_== 200
		}
	}

	"HTTP(object) GET" should {
		"simple access to yahoo.com" in {
			Http.get(yahoo_com).statusCode must_== 200
		}

		"simple access to google.co.jp" in {
			Http.get(google_co_jp).statusCode must_== 200
		}
	}

	"confirm User-Agent" should {
		"chrome" in {
			val http = Http().userAgent(UserAgent.CHROME_20)
			http.get(google_co_jp).statusCode must_== 200
		}
	}

}
