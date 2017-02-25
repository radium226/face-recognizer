cmake_minimum_required(VERSION 2.8.0)

project (dlibjni)

set(PROJECT_VERSION_MAJOR 0)
set(PROJECT_VERSION_MINOR 0)
set(PROJECT_VERSION_PATCH 0)

set(DLIB_INCLUDE_DIRS "/usr/include/dlib")

# Setup JNI
find_package(JNI REQUIRED)
if (JNI_FOUND)
    message (STATUS "JNI include directories: ${JNI_INCLUDE_DIRS}")
endif()

# Include directories
include_directories(.)
include_directories(include)
include_directories(${JNI_INCLUDE_DIRS})

find_package(OpenCV REQUIRED)
include_directories(${OpenCV_INCLUDE_DIRS})

# Sources
file(GLOB LIB_SRC
  "*.c"
  "*.cc"
  "*.cpp"
  "/usr/include/dlib/all/source.cpp"
)

add_definitions(-DDLIB_NO_GUI_SUPPORT)
#add_definitions(-DDLIB_PNG_SUPPORT)

link_directories(${DLIB_INCLUDE_DIRS})

# Setup installation targets
# (required by sbt-jni) major version should always be appended to library name
#
set (LIB_NAME ${PROJECT_NAME}${PROJECT_VERSION_MAJOR})
add_library(${LIB_NAME} SHARED ${LIB_SRC})
install(TARGETS ${LIB_NAME} LIBRARY DESTINATION .)
