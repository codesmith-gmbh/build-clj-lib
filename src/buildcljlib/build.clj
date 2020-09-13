(ns buildcljlib.build
  (:require [badigeon.clean :as clean]
            [badigeon.compile :as compile]
            [badigeon.bundle :as bundle])
  (:import (java.nio.file Path Files)
           (java.nio.file.attribute PosixFilePermissions FileAttribute)))

(defn clean []
  (clean/clean "target"))

(defn create-bin-file [^Path out-path main-namespace]
  (let [bin-dir-path (.resolve out-path "bin")
        script-path  (.resolve bin-dir-path "run.sh")]
    (Files/createDirectories bin-dir-path (make-array FileAttribute 0))
    (spit (.toFile script-path)
          (str
            "#!/bin/bash
DIR=\"$( cd \"$( dirname \"${BASH_SOURCE[0]}\" )\" && pwd )\"
java -cp \"${DIR}/..:${DIR}/../lib/*\" "
            main-namespace
            "\n"))
    (Files/setPosixFilePermissions script-path
                                   (PosixFilePermissions/fromString "rwxr-xr-x"))))

(defn make-bundle [main-namespace lib-name version]
  (println "cleaning")
  (clean)
  (println "compiling")
  (compile/compile main-namespace)
  (let [out-path (bundle/make-out-path lib-name version)]
    (println "bundling")
    (bundle/bundle out-path {:aliases [:aot-output]})
    (println "bin file")
    (create-bin-file out-path main-namespace))
  (println "done"))
