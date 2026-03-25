/*
 * MIT License
 *
 * Copyright (c) 2024 Rafael Fernandez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.eff3ct.teckel.api.server

import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

import com.eff3ct.teckel.api.{DocGen, DryRun, GraphViz}
import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}

object TeckelServer {

  def start(port: Int = 8080): HttpServer = {
    val server = HttpServer.create(new InetSocketAddress(port), 0)

    server.createContext(
      "/api/health",
      new HttpHandler {
        def handle(exchange: HttpExchange): Unit = {
          val response = """{"status":"ok","version":"2.0"}"""
          sendJson(exchange, 200, response)
        }
      }
    )

    server.createContext(
      "/api/pipelines/dry-run",
      new HttpHandler {
        def handle(exchange: HttpExchange): Unit = {
          if (exchange.getRequestMethod != "POST") {
            sendJson(exchange, 405, """{"error":"Method not allowed"}""")
            return
          }
          val body = readBody(exchange)
          DryRun.explain(body) match {
            case Right(plan) =>
              val json = s"""{"status":"ok","plan":"${escapeJson(plan)}"}"""
              sendJson(exchange, 200, json)
            case Left(err) =>
              sendJson(exchange, 400, s"""{"error":"${escapeJson(err.getMessage)}"}""")
          }
        }
      }
    )

    server.createContext(
      "/api/pipelines/doc",
      new HttpHandler {
        def handle(exchange: HttpExchange): Unit = {
          if (exchange.getRequestMethod != "POST") {
            sendJson(exchange, 405, """{"error":"Method not allowed"}""")
            return
          }
          val body = readBody(exchange)
          DocGen.generate(body) match {
            case Right(doc) =>
              sendText(exchange, 200, doc)
            case Left(err) =>
              sendJson(exchange, 400, s"""{"error":"${escapeJson(err.getMessage)}"}""")
          }
        }
      }
    )

    server.createContext(
      "/api/pipelines/graph",
      new HttpHandler {
        def handle(exchange: HttpExchange): Unit = {
          if (exchange.getRequestMethod != "POST") {
            sendJson(exchange, 405, """{"error":"Method not allowed"}""")
            return
          }
          val body   = readBody(exchange)
          val params = parseQuery(exchange.getRequestURI.getQuery)
          val format = params.getOrElse("format", "mermaid")
          GraphViz.generate(body, format) match {
            case Right(graph) =>
              sendText(exchange, 200, graph)
            case Left(err) =>
              sendJson(exchange, 400, s"""{"error":"${escapeJson(err.getMessage)}"}""")
          }
        }
      }
    )

    server.createContext(
      "/api/pipelines/validate",
      new HttpHandler {
        def handle(exchange: HttpExchange): Unit = {
          if (exchange.getRequestMethod != "POST") {
            sendJson(exchange, 405, """{"error":"Method not allowed"}""")
            return
          }
          val body = readBody(exchange)
          DryRun.explain(body) match {
            case Right(_) =>
              sendJson(exchange, 200, """{"valid":true,"errors":[]}""")
            case Left(err) =>
              sendJson(
                exchange,
                200,
                s"""{"valid":false,"errors":["${escapeJson(err.getMessage)}"]}"""
              )
          }
        }
      }
    )

    server.setExecutor(null) // scalastyle:ignore null
    server.start()
    println(s"[teckel-server] Started on http://localhost:$port")
    println(s"[teckel-server] Endpoints:")
    println(s"  GET  /api/health")
    println(s"  POST /api/pipelines/dry-run")
    println(s"  POST /api/pipelines/doc")
    println(s"  POST /api/pipelines/graph?format=mermaid|dot|ascii")
    println(s"  POST /api/pipelines/validate")
    server
  }

  private def readBody(exchange: HttpExchange): String = {
    val is   = exchange.getRequestBody
    val body = new String(is.readAllBytes(), StandardCharsets.UTF_8)
    is.close()
    body
  }

  private def sendJson(exchange: HttpExchange, code: Int, body: String): Unit = {
    val bytes = body.getBytes(StandardCharsets.UTF_8)
    exchange.getResponseHeaders.set("Content-Type", "application/json")
    exchange.sendResponseHeaders(code, bytes.length.toLong)
    exchange.getResponseBody.write(bytes)
    exchange.getResponseBody.close()
  }

  private def sendText(exchange: HttpExchange, code: Int, body: String): Unit = {
    val bytes = body.getBytes(StandardCharsets.UTF_8)
    exchange.getResponseHeaders.set("Content-Type", "text/plain")
    exchange.sendResponseHeaders(code, bytes.length.toLong)
    exchange.getResponseBody.write(bytes)
    exchange.getResponseBody.close()
  }

  private def escapeJson(s: String): String =
    s.replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\n", "\\n")
      .replace("\r", "\\r")

  private def parseQuery(query: String): Map[String, String] =
    Option(query)
      .map(
        _.split("&")
          .flatMap { param =>
            param.split("=", 2) match {
              case Array(k, v) => Some(k -> v)
              case _           => None
            }
          }
          .toMap
      )
      .getOrElse(Map.empty)
}
