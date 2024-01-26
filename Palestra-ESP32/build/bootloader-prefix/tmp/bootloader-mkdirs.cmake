# Distributed under the OSI-approved BSD 3-Clause License.  See accompanying
# file Copyright.txt or https://cmake.org/licensing for details.

cmake_minimum_required(VERSION 3.5)

file(MAKE_DIRECTORY
  "/home/michaelg/SUNYPoly/esp-idf/components/bootloader/subproject"
  "/home/michaelg/SUNYPoly/S24/capstone/Palestra-ESP32/build/bootloader"
  "/home/michaelg/SUNYPoly/S24/capstone/Palestra-ESP32/build/bootloader-prefix"
  "/home/michaelg/SUNYPoly/S24/capstone/Palestra-ESP32/build/bootloader-prefix/tmp"
  "/home/michaelg/SUNYPoly/S24/capstone/Palestra-ESP32/build/bootloader-prefix/src/bootloader-stamp"
  "/home/michaelg/SUNYPoly/S24/capstone/Palestra-ESP32/build/bootloader-prefix/src"
  "/home/michaelg/SUNYPoly/S24/capstone/Palestra-ESP32/build/bootloader-prefix/src/bootloader-stamp"
)

set(configSubDirs )
foreach(subDir IN LISTS configSubDirs)
    file(MAKE_DIRECTORY "/home/michaelg/SUNYPoly/S24/capstone/Palestra-ESP32/build/bootloader-prefix/src/bootloader-stamp/${subDir}")
endforeach()
if(cfgdir)
  file(MAKE_DIRECTORY "/home/michaelg/SUNYPoly/S24/capstone/Palestra-ESP32/build/bootloader-prefix/src/bootloader-stamp${cfgdir}") # cfgdir has leading slash
endif()
