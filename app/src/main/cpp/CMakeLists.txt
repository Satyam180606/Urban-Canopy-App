cmake_minimum_required(VERSION 3.22.1)

project("urbancanopy")

add_library(urbancanopy SHARED
        game_engine.cpp)

find_library(log-lib log)

target_link_libraries(urbancanopy
        ${log-lib}
        android)
