JAVA_HOME := /usr/lib/jvm/java-17-openjdk
ANDROID_HOME := $(HOME)/Android/Sdk
GRADLEW := JAVA_HOME=$(JAVA_HOME) ANDROID_HOME=$(ANDROID_HOME) ./gradlew

.PHONY: build install clean test lint

build:
	$(GRADLEW) assembleDebug

install:
	$(GRADLEW) installDebug

test:
	$(GRADLEW) testDebugUnitTest

lint:
	$(GRADLEW) lintDebug

clean:
	$(GRADLEW) clean
