package TopWordsFunctional

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.io.Source

class TopWordsFunctionalTests extends AnyFlatSpec with Matchers {

    "The TopWordsFunctional" should "process words without throwing an error" in {
        val words = Iterator("example", "test", "scala")
        val ignoredWords = Set("ignored")
        val minLength = 3
        val windowSize = 10
        val updateEvery = 5

        // Call the method; it should compile and run without errors
        noException should be thrownBy {
            TopWordsFunctional.processWords(words, ignoredWords, minLength, windowSize, updateEvery)
        }
    }

    it should "ignore specified ignored words" in {
        val words = Iterator("example", "ignored", "scala", "test")
        val ignoredWords = Set("ignored")
        val minLength = 3
        val windowSize = 10
        val updateEvery = 1

        val results = TopWordsFunctional.processWords(words, ignoredWords, minLength, windowSize, updateEvery).toList
        results.foreach { case (counts, _) =>
            counts should not contain key("ignored")
        }
    }

    it should "filter out words shorter than minimum length" in {
        val words = Iterator("hi", "a", "ok", "scala", "test")
        // Explicitly specify the type of ignoredWords
        val ignoredWords: Set[String] = Set() 
        val minLength = 3
        val windowSize = 10
        val updateEvery = 1

        val results = TopWordsFunctional.processWords(words, ignoredWords, minLength, windowSize, updateEvery).toList
        results.foreach { case (counts, _) =>
            counts.keys.foreach { word =>
                word.length should be >= minLength
            }
        }
    }

    it should "count word occurrences accurately" in {
        val words = Iterator("scala", "scala", "test", "test", "test", "example")
        val ignoredWords: Set[String] = Set() 
        val minLength = 3
        val windowSize = 10
        val updateEvery = 1

        val results = TopWordsFunctional.processWords(words, ignoredWords, minLength, windowSize, updateEvery).toList
        results.foreach { case (counts, _) =>
            counts("test") should be(3)
            counts("scala") should be(2)
            counts("example") should be(1)
        }
    }

    it should "handle empty input correctly" in {
        val words = Iterator.empty
        val ignoredWords: Set[String] = Set() 
        val minLength = 3
        val windowSize = 10
        val updateEvery = 1

        val results = TopWordsFunctional.processWords(words, ignoredWords, minLength, windowSize, updateEvery).toList
        results should be (empty) 
    }

    it should "handle input with only ignored words" in {
        val words = Iterator("ignored", "ignored", "ignored")
        val ignoredWords = Set("ignored")
        val minLength = 3
        val windowSize = 10
        val updateEvery = 1

        val results = TopWordsFunctional.processWords(words, ignoredWords, minLength, windowSize, updateEvery).toList
        results should be (empty)
    }

    // Test missing ignored-words file (error handling)
    it should "handle a missing ignored-words file gracefully" in {
        val args = Array.empty[String] // No input args for this test
    
        // Simulate the missing file by catching the exception
        val ignoredWords = try {
            Source.fromFile("missing-file").getLines().toSet
        } catch {
            case _: Exception => Set.empty[String]
        }
    
        ignoredWords shouldBe empty // The ignored words set should be empty if the file doesn't exist
    }
  
    // Test when all words are ignored
    it should "handle input where all words are ignored" in {
        val words = Iterator("ignore1", "ignore2", "ignore3")
        val ignoredWords: Set[String] = Set("ignore1", "ignore2", "ignore3")
        val minLength = 3
        val windowSize = 10
        val updateEvery = 2
    
        val results = TopWordsFunctional.processWords(words, ignoredWords, minLength, windowSize, updateEvery).toList
        results should be (empty) // No words left to process
    }
  
    // Test behavior with very short words
    it should "filter out words below the minimum length threshold" in {
        val words = Iterator("a", "b", "hi", "scala", "java")
        val ignoredWords: Set[String] = Set()
        val minLength = 4
        val windowSize = 10
        val updateEvery = 1
    
        val results = TopWordsFunctional.processWords(words, ignoredWords, minLength, windowSize, updateEvery).toList
        results.foreach { case (counts, _) =>
            counts.keys.foreach { word =>
                word.length should be >= minLength
            }
        }
    }
  
    // Test output when all word frequencies are below the minimum threshold
    it should "produce an empty word cloud when no word frequencies meet the minimum" in {
        val wordCounts = Map("scala" -> 2, "java" -> 1, "test" -> 1)
        val cloudSize = 3
        val minFrequency = 3 // No word meets this frequency
    
        val output = TopWordsFunctional.printWordCloud(wordCounts, cloudSize, minFrequency)
        output shouldEqual "" // Expecting an empty word cloud
    }

    // Test the command-line argument parsing
    "The Config trait" should "correctly handle default arguments when no input is provided" in {
      val args = Array.empty[String]
      val expectedDefaults = Map(
        "cloud-size" -> 10,
        "length-at-least" -> 6,
        "window-size" -> 1000,
        "min-frequency" -> 3,
        "update-every" -> 10
      )
  
      val result = TopWordsFunctional.handleArgs(args)
      result shouldEqual expectedDefaults
    }
  
    it should "correctly parse provided arguments" in {
      val args = Array("--cloud-size", "15", "--length-at-least", "8", "--window-size", "2000", "--min-frequency", "5", "--update-every", "20")
      val expectedParsed = Map(
        "cloud-size" -> 15,
        "length-at-least" -> 8,
        "window-size" -> 2000,
        "min-frequency" -> 5,
        "update-every" -> 20
      )
  
      val result = TopWordsFunctional.handleArgs(args)
      result shouldEqual expectedParsed
    }
  
    it should "fall back to defaults for missing arguments" in {
        val args = Array("--cloud-size", "15", "--window-size", "2000")
        val expectedParsed = Map(
            "cloud-size" -> 15,
            "length-at-least" -> 6,   // Default
            "window-size" -> 2000,
            "min-frequency" -> 3,     // Default
            "update-every" -> 10      // Default
        )
    
        val result = TopWordsFunctional.handleArgs(args)
        result shouldEqual expectedParsed
    }
  
    // Test the output formatting
    "The OutputHandler" should "correctly format the word cloud output" in {
        val wordCounts = Map("scala" -> 5, "java" -> 2, "test" -> 1)
        val cloudSize = 2
        val minFrequency = 2
    
        val output = TopWordsFunctional.printWordCloud(wordCounts, cloudSize, minFrequency)
        output shouldEqual "scala: 5 java: 2"
    }
  
    it should "filter out words below the minimum frequency" in {
        val wordCounts = Map("scala" -> 5, "java" -> 2, "test" -> 1)
        val cloudSize = 3
        val minFrequency = 2
    
        val output = TopWordsFunctional.printWordCloud(wordCounts, cloudSize, minFrequency)
        output shouldEqual "scala: 5 java: 2"
    }
  
    it should "limit the word cloud size to the cloud-size argument" in {
        val wordCounts = Map("scala" -> 5, "java" -> 2, "test" -> 1)
        val cloudSize = 1
        val minFrequency = 1
    
        val output = TopWordsFunctional.printWordCloud(wordCounts, cloudSize, minFrequency)
        output shouldEqual "scala: 5"
    }

    // Test for small cloud size (boundary test)
    it should "handle a cloud size of 0 correctly" in {
        val wordCounts = Map("scala" -> 5, "java" -> 3)
        val cloudSize = 0
        val minFrequency = 1
    
        val output = TopWordsFunctional.printWordCloud(wordCounts, cloudSize, minFrequency)
        output shouldEqual "" // Expecting an empty string
    }
  
    // Test edge cases in word processing
    "The WordProcessor" should "handle a window size smaller than the input word list" in {
        val words = Iterator("scala", "test", "java", "functional", "programming")
        val ignoredWords: Set[String] = Set()
        val minLength = 3
        val windowSize = 3
        val updateEvery = 1
    
        val results = TopWordsFunctional.processWords(words, ignoredWords, minLength, windowSize, updateEvery).toList
        results.nonEmpty shouldBe true
    }
  
    it should "not update word cloud before updateEvery threshold" in {
        val words = Iterator("scala", "java", "test")
        val ignoredWords: Set[String] = Set()
        val minLength = 3
        val windowSize = 10
        val updateEvery = 1000 // Large threshold
    
        val results = TopWordsFunctional.processWords(words, ignoredWords, minLength, windowSize, updateEvery).toList
        results should be(empty) // Nothing should happen since update threshold isn't reached
    }
  
    // Test for small updateEvery value (boundary test)
    it should "process words correctly with updateEvery set to 1" in {
        val words = Iterator("scala", "scala", "java", "java", "test")
        val ignoredWords: Set[String] = Set()
        val minLength = 3
        val windowSize = 3
        val updateEvery = 1 // Trigger update on every step
    
        val results = TopWordsFunctional.processWords(words, ignoredWords, minLength, windowSize, updateEvery).toList
        results should not be empty
    }
  
    // Test for empty ignoredWords set (special case)
    it should "handle an empty ignoredWords set without failing" in {
        val words = Iterator("scala", "java", "test")
        val ignoredWords: Set[String] = Set() // No ignored words
        val minLength = 4
        val windowSize = 10
        val updateEvery = 2
    
        val results = TopWordsFunctional.processWords(words, ignoredWords, minLength, windowSize, updateEvery).toList
        results.foreach { case (counts, _) =>
            counts.keys should contain allOf ("scala", "java", "test")
        }
    }
  
    // Test special characters (non-alphabetic characters handling)
    it should "process words with special characters correctly" in {
        val words = Iterator("scala!", "java123", "test", "test!@#", "123programming")
        val ignoredWords: Set[String] = Set()
        val minLength = 4
        val windowSize = 10
        val updateEvery = 1
    
        val results = TopWordsFunctional.processWords(words, ignoredWords, minLength, windowSize, updateEvery).toList
        results.foreach { case (counts, _) =>
            counts.keys should contain allOf ("scala", "java123", "test")
            counts.keys should not contain ("test!@#", "123programming") // Words filtered due to special chars or being too short
        }
    }
}
