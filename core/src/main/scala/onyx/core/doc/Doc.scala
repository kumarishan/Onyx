package onyx.core.doc

import java.io.{BufferedInputStream, InputStream, ByteArrayInputStream}

import org.apache.hadoop.io.{Text, BytesWritable}

trait Doc {

}

trait HadoopTypeImplicits {

  implicit def text2InputStream(s: Text): InputStream =
    new ByteArrayInputStream(s.getBytes)

  implicit def bytesWritable2InputStream(s: BytesWritable) =
    new ByteArrayInputStream(s.getBytes)

}