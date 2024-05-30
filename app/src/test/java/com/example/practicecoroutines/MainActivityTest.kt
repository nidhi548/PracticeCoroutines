package com.example.practicecoroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

class MainActivityTest {
    @Test
    fun `executing suspending functions sequentially`(): Unit = runBlocking {
        val startTime = System.currentTimeMillis()
        dummyTask1()
        dummyTask2()
        val endTime = System.currentTimeMillis()
        println("Total time taken: ${(endTime - startTime) / 1000} secs")
    }

    @Test
    fun `executing suspending function in parallel` ()= runBlocking {
        val scope = CoroutineScope(context = Job())

        val startTime = System.currentTimeMillis()
        val job1 = scope.launch { dummyTask1() }
        val job2 = scope.launch { dummyTask2() }
        job1.join()
        job2.join()
        val endTime = System.currentTimeMillis()
        println("Total time taken: ${(endTime - startTime) / 1000} secs")
    }

    private suspend fun dummyTask1() {
        for (i in 1..5) {
            println("Dummy task 1: $i")
            delay(1000)
        }
        println("Dummy task 1 done")
    }

    private suspend fun dummyTask2() {
        for (i in 1..5) {
            println("Dummy task 2: $i")
            delay(1000)
        }
        println("Dummy task 2 done")
    }

    @Test
    fun `executing suspending functions with return type in parallel`() = runBlocking {
        val scope = CoroutineScope(context = Job())

        val startTime = System.currentTimeMillis()
        val job1 = scope.async {
            dummyTask3()
        }
        val job2 = scope.async {
            dummyTask4()
        }
        val sumTotal = job1.await() + job2.await()
        val endTime = System.currentTimeMillis()
        println("Total time taken: ${(endTime - startTime) / 1000} secs")
        println("Sum total: $sumTotal")
    }

    private suspend fun dummyTask3(): Int {
        var sum = 0
        for (i in 1..5) {
            println("Dummy task 3: $i")
            delay(1000)
            sum += i
        }
        println("Dummy task 3 done")
        return sum
    }

    private suspend fun dummyTask4(): Int {
        var sum = 0
        for (i in 1..5) {
            println("Dummy task 4: $i")
            delay(1000)
            sum += i
        }
        println("Dummy task 4 done")
        return sum
    }

    @Test
    fun `completion of a job doesn't wait by itself`() = runBlocking {
        val scope = CoroutineScope(context = Job())

        val startTime = System.currentTimeMillis()
        scope.launch {
            dummyTask5()
        }

        val endTime = System.currentTimeMillis()
        println("Total time taken: ${(endTime - startTime) / 1000} secs")
    }

    private suspend fun dummyTask5() {
        println("Starting dummy task 5")
        delay(5000)
        println("Dummy task 5 done")
    }

    @Test
    fun `you have to ensure completion of a job`() = runBlocking {
        val scope = CoroutineScope(context = Job())

        val startTime = System.currentTimeMillis()
        val job = scope.launch {
            dummyTask5()
        }

        job.join()

        val endTime = System.currentTimeMillis()
        println("Total time taken: ${(endTime - startTime) / 1000} secs")
    }

    @Test
    fun `be careful where to wait for result from a suspending function`() = runBlocking {
        val scope = CoroutineScope(context = Job())

        val startTime = System.currentTimeMillis()
        val job1 = scope.launch {
            dummyTask1()
        }
        job1.join()

        val job2 = scope.launch {
            dummyTask2()
        }
        job2.join()

        val endTime = System.currentTimeMillis()
        println("Total time taken: ${(endTime - startTime) / 1000} secs")
    }

    @Test
    fun `after how long will the function evaluation complete using launch`() = runBlocking {
        val scope = CoroutineScope(context = Job())
        val startTime = System.currentTimeMillis()

        val job1 = scope.launch {
            delay(5000)
            val endTime = System.currentTimeMillis()
            println("Task 1 completed at: ${(endTime - startTime) / 1000} secs")
        }

        val job2 = scope.launch {
            delay(3000)
            val endTime = System.currentTimeMillis()
            println("Task 2 completed at: ${(endTime - startTime) / 1000} secs")
        }
        job1.join()
        job2.join()
        val endTime = System.currentTimeMillis()
        println("Total time taken: ${(endTime - startTime) / 1000} secs")
    }

    @Test
    fun `after how long will the function evaluation complete part 2 using launch`() = runBlocking {
        val scope = CoroutineScope(context = Job())
        val startTime = System.currentTimeMillis()

        val job1 = scope.launch {
            delay(5000)
            val endTime = System.currentTimeMillis()
            println("Task 1 completed at: ${(endTime - startTime) / 1000} secs")
        }

        val job2 = scope.launch {
            delay(3000)
            val endTime = System.currentTimeMillis()
            println("Task 2 completed at: ${(endTime - startTime) / 1000} secs")
        }
        job2.join()
        job1.join()
        val endTime = System.currentTimeMillis()
        println("Total time taken: ${(endTime - startTime) / 1000} secs")
    }

    @Test
    fun `after how long will the function evaluation complete using async`() = runBlocking {
        val scope = CoroutineScope(context = Job())
        val startTime = System.currentTimeMillis()

        val job1 = scope.async {
            delay(5000)
            val endTime = System.currentTimeMillis()
            "Task 1 completed at: ${(endTime - startTime) / 1000} secs"
        }

        val job2 = scope.async {
            delay(3000)
            val endTime = System.currentTimeMillis()
            "Task 2 completed at: ${(endTime - startTime) / 1000} secs"
        }
        println(job1.await())
        println(job2.await())
        val endTime = System.currentTimeMillis()
        println("Total time taken: ${(endTime - startTime) / 1000} secs")
    }

    @Test
    fun `time calculation for await all on async`() = runBlocking {
        val scope = CoroutineScope(context = Job())
        val startTime = System.currentTimeMillis()

        val jobList = mutableListOf<Deferred<Int>>()

        jobList.add(scope.async {
            delay(5000)
            1
        })

        jobList.add(scope.async {
            delay(9000)
            2
        })

        jobList.add(scope.async {
            delay(7000)
            3
        })

        val sum = jobList.awaitAll().sum()
        val endTime = System.currentTimeMillis()
        println("Sum: $sum")
        println("Total time taken: ${(endTime - startTime) / 1000} secs")
    }

    @Test
    fun `predicting time when await will get executed`() = runBlocking {
        val scope = CoroutineScope(context = Job())
        val startTime = System.currentTimeMillis()

        val job1 = scope.async {
            delay(5000)
            1
        }

        val job2 = scope.async {
            delay(9000)
            2
        }

        val job3 = scope.async {
            delay(7000)
            3
        }

        val job1Result = job1.await()
        println("Got job1Result after ${(System.currentTimeMillis() - startTime) / 1000} secs")

        val job2Result = job2.await()
        println("Got job2Result after ${(System.currentTimeMillis() - startTime) / 1000} secs")

        val job3Result = job3.await()
        println("Got job3Result after ${(System.currentTimeMillis() - startTime) / 1000} secs")

        val endTime = System.currentTimeMillis()
        println("Total time taken: ${(endTime - startTime) / 1000} secs")
    }

    @Test
    fun `predicting time when await will get executed part 2`() = runBlocking {
        val scope = CoroutineScope(context = Job())
        val startTime = System.currentTimeMillis()

        val job1 = scope.async {
            delay(5000)
            1
        }

        val job2 = scope.async {
            delay(9000)
            2
        }

        val job3 = scope.async {
            delay(7000)
            3
        }

        val job2Result = job2.await()
        println("Got job2Result after ${(System.currentTimeMillis() - startTime) / 1000} secs")

        val job3Result = job3.await()
        println("Got job3Result after ${(System.currentTimeMillis() - startTime) / 1000} secs")

        val job1Result = job1.await()
        println("Got job1Result after ${(System.currentTimeMillis() - startTime) / 1000} secs")

        val endTime = System.currentTimeMillis()
        println("Total time taken: ${(endTime - startTime) / 1000} secs")
    }

    @Test
    fun `how to optimise the total execution time`() = runBlocking {

        val customerId = "123"

        val fetchFirstName: suspend (String) -> String = { id ->
            delay(5000)
            if (id == "123") {
                "FirstName"
            } else {
                "FirstName2"
            }
        }

        val fetchMiddleName: suspend (String) -> String = { id ->
            delay(9000)
            if (id == "123") {
                "MiddleName"
            } else {
                "MiddleName2"
            }
        }

        val fetchLastName: suspend (String) -> String = { id ->
            delay(7000)
            if (id == "123") {
                "LastName"
            } else {
                "LastName2"
            }
        }

        val fetchContactNumber: suspend (String) -> String = { id ->
            delay(3000)
            if (id == "123") {
                "ContactNumber"
            } else {
                "ContactNumber2"
            }
        }

        val finalName: suspend (String, String, String) -> String =
            { firstName, middleName, lastName ->
                delay(3000)
                "Mrs. $firstName $middleName $lastName"
            }

        val startTime = System.currentTimeMillis()

        val nameFetchDeferredList = mutableListOf<Deferred<String>>()
        nameFetchDeferredList.add(async { fetchFirstName(customerId) })
        nameFetchDeferredList.add(async { fetchMiddleName(customerId) })
        nameFetchDeferredList.add(async { fetchLastName(customerId) })

        val contactNumberDeferred = async { fetchContactNumber(customerId) }

        // At this point, all the above tasks are running in parallel

        val finalNameDeferred = async {
            val nameList = nameFetchDeferredList.awaitAll()
            finalName(nameList[0], nameList[1], nameList[2])
        }

        val finalNameResult = finalNameDeferred.await()
        val contactNumberResult = contactNumberDeferred.await()

        val endTime = System.currentTimeMillis()
        println("Total time taken: ${(endTime - startTime) / 1000} secs")
    }
}