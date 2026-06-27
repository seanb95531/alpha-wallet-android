cmake_minimum_required(VERSION 3.4.1)

add_library(keys SHARED
            keys.c)

# Ensure output .so is linked with 16KB page-size compatibility.
set_target_properties(keys PROPERTIES
    LINK_FLAGS "-Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=16384")

# Include libraries needed for native-lib lib
target_link_libraries(keys
                      android
                      log)
