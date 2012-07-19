package com.github.teaplanet.http

import org.specs2.mutable._

/**
 * Http client spec.
 *
 * Author: Ken
 * Date: 2012-07-05 0:45
 */
class HttpSpec extends Specification {

	"HTTP GET" should {
		"simple access to yahoo.com" in {
			val yahoo = "http://www.yahoo.com/"
			Http.get(yahoo).statusCode must_== 200
		}

		"simple access to google.co.jp" in {
			val google = "http://www.google.co.jp/"
			Http.get(google).statusCode must_== 200
		}
	}

}
