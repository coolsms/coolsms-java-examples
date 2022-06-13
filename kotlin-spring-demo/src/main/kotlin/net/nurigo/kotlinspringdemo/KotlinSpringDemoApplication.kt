package net.nurigo.kotlinspringdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KotlinSpringDemoApplication

fun main(args: Array<String>) {
	runApplication<KotlinSpringDemoApplication>(*args)
}
