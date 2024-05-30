package com.example.practicecoroutines

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import org.junit.Test
import kotlinx.coroutines.test.runTest


class CoroutineCancellation {
    @Test
    fun `cancelling scope cancels all its children`() = runBlocking {
        val scope = CoroutineScope(context = Job())

        val job1 = scope.launch {
            dummyTask1()
        }
        println("Job 1 is launched")

        val job2 = scope.launch {
            dummyTask2()
        }
        println("Job 2 is launched")

        delay(5000)

        println("Cancelling scope")
        scope.cancel()
        job1.join()
        job2.join()
    }

    private suspend fun dummyTask1() {
        for (i in 1..10) {
            println("Dummy task 1: $i")
            delay(1000)
        }
        println("Dummy task 1 done")
    }

    private suspend fun dummyTask2() {
        for (i in 1..10) {
            println("Dummy task 2: $i")
            delay(1000)
        }
        println("Dummy task 2 done")
    }

    @Test
    fun `cancelling child doesn't affect other siblings`() = runBlocking {
        val scope = CoroutineScope(context = Job())

        val job1 = scope.launch {
            dummyTask1()
        }
        println("Job 1 is launched")

        val job2 = scope.launch {
            dummyTask2()
        }
        println("Job 2 is launched")

        delay(5000)

        println("Cancelling job 1")
        job1.cancel()

        job1.join()
        job2.join()
    }

    @Test
    fun `once a scope is cancelled you can't launch a new coroutine in that scope and it fails silently`() =
        runBlocking {
            val scope = CoroutineScope(context = Job())

            val job1 = scope.launch {
                dummyTask1()
            }
            println("Job 1 is launched")

            val job2 = scope.launch {
                dummyTask2()
            }
            println("Job 2 is launched")

            delay(5000)

            println("Cancelling scope")
            scope.cancel()

            val job3 = scope.launch {
                dummyTask3()
            }

            job1.join()
            job2.join()
            job3.join()
        }

    private suspend fun dummyTask3() {
        for (i in 1..10) {
            println("Dummy task 3: $i")
            delay(1000)
        }
        println("Dummy task 3 done")
    }

    @Test
    fun `job cancel function's break failed`() = runBlocking {

        val scope = CoroutineScope(context = Job())
        val totalDurationInSeconds = 10

        val job = scope.launch {
            val startTime = System.currentTimeMillis()
            var nextPrintTime = startTime
            var i = 0
            while (i < totalDurationInSeconds) {
                // print a message every second for 10 seconds
                if (System.currentTimeMillis() >= nextPrintTime) {
                    println("Dummy task 4: ${++i}")
                    nextPrintTime += 1000L
                }
            }
        }
        println("Job is launched")

        delay(5000)
        println("5 seconds wait is completed. Now cancelling job")
        job.cancel()
        println("Job cancelled. Done!")
        job.join()
    }

    @Test
    fun `fixing job cancel function's break & making it disc break`() = runBlocking {

        val scope = CoroutineScope(context = Job())
        val totalDurationInSeconds = 10

        val job = scope.launch {
            val startTime = System.currentTimeMillis()
            var nextPrintTime = startTime
            var i = 0
            while (i < totalDurationInSeconds && isActive) {
                // print a message every second for 10 seconds
                if (System.currentTimeMillis() >= nextPrintTime) {
                    println("Dummy task 4: ${++i}")
                    nextPrintTime += 1000L
                }
            }
        }
        println("Job is launched")

        delay(5000)
        println("5 seconds wait is completed. Now cancelling job")
        job.cancel()
        println("Job cancelled. Done!")
        job.join()
    }

    @Test
    fun `a child throwing an exception propagates the error & cancels other children`() = runTest {
        val scope = CoroutineScope(context = Job())

        val job1 = scope.launch {
            dummyTask11()
        }

        val job2 = scope.launch {
            dummyTask2()
        }

        job1.join()
        job2.join()
    }

    private suspend fun dummyTask11() {
        println("Dummy task 1 started")
        delay(5000)
        throw Exception("Dummy task 1 failed")
    }

    @Test
    fun `a child throwing an exception should not propagate error & other children should continue working`() =
        runTest {
            val scope = CoroutineScope(context = SupervisorJob())

            val job1 = scope.launch {
                dummyTask11()
            }

            val job2 = scope.launch {
                dummyTask2()
            }

            val job3 = scope.launch {
                dummyTask3()
            }

            job1.join()
            job2.join()
            job3.join()
        }

    @Test
    fun `a child throwing an exception should not propagate error & other children should continue working using supervisor scope`() =
        runTest {
            val scope = CoroutineScope(context = Job())

            val job = scope.launch {
                supervisorScope {
                    launch {
                        dummyTask11()
                    }
                    launch {
                        dummyTask2()
                    }
                    launch {
                        dummyTask3()
                    }
                }

            }

            job.join()
        }
    @Test
    fun `SuperVisorJob should be part of scope, being part of coroutine builder does nothing`() =
        runTest {
            val scope = CoroutineScope(context = Job())

            val job = scope.launch(SupervisorJob()) {
                launch {
                    dummyTask11()
                }
                launch {
                    dummyTask2()
                }
                launch {
                    dummyTask3()
                }
            }

            job.join()
        }

    @Test
    fun `launch throws exception as soon as they happen so it needs to be handled at launch itself else it fails`() =
        runTest {
            val scope = CoroutineScope(context = Job())
            val job = scope.launch {
                dummyTask12()
            }

            try {
                job.join()
            } catch (e: Exception) {
                println("Caught exception in join block with message: ${e.message}")
            }
        }

    private suspend fun dummyTask12() {
        println("Dummy task 1 started")
        delay(5000)
        throw Exception("Dummy task 1 failed")
    }

    @Test
    fun `launch throws exception as soon as they happen so it needs to be handled at launch itself`() =
        runTest {
            val scope = CoroutineScope(context = Job())
            val job = scope.launch {
                try {
                    dummyTask12()
                } catch (e: Exception) {
                    println("Caught exception in launch block with message: ${e.message}")
                }
            }

            try {
                job.join()
            } catch (e: Exception) {  // This is useless
                println("Caught exception in join block with message: ${e.message}")
            }
        }

    @Test
    fun `async throws exception when await is called so can be handled later at await`() = runTest {
        val scope = CoroutineScope(context = Job())
        val job = scope.async {
            dummyTask12()
        }

        try {
            job.await()
        } catch (e: Exception) {
            println("Caught exception in await block with message: ${e.message}")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `async throws exception when await is called but can be handled at async as well`() =
        runTest {
            val scope = CoroutineScope(context = SupervisorJob())
            val job = scope.async {
                try {
                    dummyTask12()
                } catch (e: Exception) {
                    println("Caught exception in async block with message: ${e.message}")
                }
            }

            try {
                job.await()
            } catch (e: Exception) {
                println("Caught exception in await block with message: ${e.message}")
            }
        }

    @Test
    fun `coroutine exception handler to catch exception`() = runTest {
        val scope = CoroutineScope(context = Job())
        val handler = CoroutineExceptionHandler { coroutineContext, exception ->
            println("Caught exception in custom handler with message: ${exception.message}")
        }

        val job = scope.launch(handler) {
            dummyTask12()
        }

        job.join()
    }

    @Test
    fun `coroutine exception handler to catch exception at scope creation level`() = runTest {
        val handler = CoroutineExceptionHandler { coroutineContext, exception ->
            println("Caught exception in custom handler with message: ${exception.message}")
        }
        val scope = CoroutineScope(context = Job() + handler)

        val job = scope.launch {
            dummyTask12()
        }

        job.join()
    }

    @Test
    fun `run blocking waits for all coroutines that are launched in this scope`() = runBlocking {
        val startTime = System.currentTimeMillis()
        launch {
            dummyTask1(startTime = startTime)
        }

        launch {
            dummyTask2(startTime = startTime)
        }
        val endTime = System.currentTimeMillis()
        println("Total time taken: ${(endTime - startTime) / 1000}")
    }

    private suspend fun dummyTask1(startTime: Long) {
        println("Starting dummy task 1")
        delay(3000)
        println("Finishing dummy task 1 at ${(System.currentTimeMillis() - startTime) / 1000}")
    }

    private suspend fun dummyTask2(startTime: Long) {
        println("Starting dummy task 2")
        delay(5000)
        println("Finishing dummy task 2 at ${(System.currentTimeMillis() - startTime) / 1000}")
    }

    @Test
    fun `run test waits for all coroutines that are launched in this scope and skips all delays`() =
        runTest {
            val startTime = System.currentTimeMillis()
            launch {
                dummyTask1(startTime = startTime)
            }

            launch {
                dummyTask2(startTime = startTime)
            }
            val endTime = System.currentTimeMillis()
            println("Total time taken: ${(endTime - startTime) / 1000}")
        }
}