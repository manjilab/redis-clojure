(add-classpath "file:///Users/ragge/Projects/clojure/redis-clojure/src/")

(ns redis
  (:use redis.internal))


(defmacro with-server
  "Evaluates body in the context of a new connection to a Redis server
  then closes the connection."
  [server-spec & body]
  `(with-server* ~server-spec (fn []
                                (do
                                  (redis/select (:db *server*))
                                  ~@body))))


;;
;; Reply conversion functions
;;
(defn int-to-bool
  "Convert integer reply to a boolean value"
  [int]
  (= 1 int))

(defn string-to-type
  "Convert a string reply to a Redis type keyword,
  either :none, :string, :list or :set"
  [string]
  (keyword string))

(defn string-to-seq
  "Convert a space separated string to a sequence of words"
  [#^String string]
  (if (empty? string)
    nil
    (seq (.split string "\\s+"))))

(defn string-to-map
  "Convert strings with format 'key:value\r\n'+ to a map with {key
  value} pairs"
  [#^String string]
  (let [lines (.split string "(\\r\\n|:)")]
    (apply hash-map lines)))

;;
;; Commands
;;
(defcommands
  ;; Connection handling
  (quit      [] :inline)
  (ping      [] :inline)
  ;; String commands
  (set       [key value] :bulk)
  (get       [key] :inline)
  (getset    [key value] :bulk)
  (setnx     [key value] :bulk int-to-bool)
  (incr      [key] :inline)
  (incrby    [key integer] :inline)
  (decr      [key] :inline)
  (decrby    [key integer] :inline)
  (exists    [key] :inline int-to-bool)
  (mget      [key & keys] :inline)
  (del       [key] :inline int-to-bool)
  ;; Key space commands
  (type      [key] :inline string-to-type)
  (keys      [pattern] :inline string-to-seq)
  (randomkey [] :inline)
  (rename    [oldkey newkey] :inline)
  (renamenx  [oldkey newkey] :inline int-to-bool)
  (dbsize    [] :inline)
  (expire    [key seconds] :inline int-to-bool)
  (ttl       [key] :inline)
  ;; List commands
  (rpush     [key value] :bulk)
  (lpush     [key value] :bulk)
  (llen      [key] :inline)
  (lrange    [key start end] :inline)
  (ltrim     [key start end] :inline)
  (lindex    [key index] :inline)
  (lset      [key index value] :bulk)
  (lrem      [key count value] :bulk)
  (lpop      [key] :inline)
  (rpop      [key] :inline)
  ;; Set commands
  (sadd      [key member] :bulk int-to-bool)
  (srem      [key member] :bulk int-to-bool)
  (smove     [srckey destkey member] :bulk int-to-bool)
  (sismember [key member] :bulk int-to-bool)
  ;; Multiple database handling commands
  (select    [index] :inline)
  (move      [key dbindex] :inline)
  (flushdb   [] :inline)
  (flushall  [] :inline)
  ;; Sorting
  
  ;;
  (info      [] :inline string-to-map)
  (monitor   [] :inline))

