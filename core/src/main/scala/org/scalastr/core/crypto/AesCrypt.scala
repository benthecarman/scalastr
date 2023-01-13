package org.scalastr.core.crypto

import org.bitcoins.crypto._
import scodec.bits.ByteVector

import javax.crypto.{BadPaddingException, Cipher}
import javax.crypto.spec.IvParameterSpec
import scala.util.{Failure, Success, Try}

/** Provides functionality for encrypting and decrypting with AES
  */
object AesCrypt {

  /** AES encryption with CFB block cipher mode and no padding, such that
    * arbitrary plaintexts can be encrypted.
    */
  private val aesCipherType: String = "AES/CBC/PKCS5Padding"

  private def getCipher: Cipher = {
    val cipher = Cipher.getInstance(aesCipherType)
    cipher
  }

  private def decryptionCipher(
      secret: AesKey,
      initializationVector: AesIV): Cipher = {
    val cipher = getCipher
    cipher.init(Cipher.DECRYPT_MODE,
                secret.toSecretKey,
                new IvParameterSpec(initializationVector.bytes.toArray))
    cipher
  }

  /** Decrypts the provided data
    */
  def decrypt(
      encrypted: AesEncryptedData,
      key: AesKey): Either[AesDecryptionException, ByteVector] = {
    val cipher = decryptionCipher(key, encrypted.iv)

    val decryptionAttempt = Try {
      val plainText = cipher.doFinal(encrypted.cipherText.toArray)
      ByteVector(plainText)
    }

    decryptionAttempt match {
      case Success(bytes) => Right(bytes)
      // here we assume that bad padding is because of a bad password
      // provided. assuming that our implementation is correct, correct
      // passwords should never to lead to bad padding
      case Failure(_: BadPaddingException) =>
        Left(AesException.BadPasswordException)
      case Failure(exception) => throw exception
    }
  }

  private def encryptionCipher(secret: AesKey, iv: AesIV): Cipher = {
    val cipher = getCipher
    cipher.init(Cipher.ENCRYPT_MODE,
                secret.toSecretKey,
                new IvParameterSpec(iv.bytes.toArray))
    cipher
  }

  /** Encrypts the given plaintext, by explicitly passing in a intialization
    * vector. This is unsafe if the user passes in a bad IV, so this method is
    * kept private within Bitcoin-S. It is useful for testing purposes, so
    * that's why we expose it in the first place.
    */
  private[crypto] def encryptWithIV(
      plainText: ByteVector,
      iv: AesIV,
      key: AesKey): AesEncryptedData = {
    val cipher = encryptionCipher(key, iv)

    val cipherText = cipher.doFinal(plainText.toArray)

    val encrypted =
      AesEncryptedData(cipherText = ByteVector(cipherText), iv = iv)
    encrypted

  }

  /** Encrypts the given plaintext with the given key.
    */
  def encrypt(plainText: ByteVector, key: AesKey): AesEncryptedData = {
    val iv = AesIV.random
    encryptWithIV(plainText, iv, key)
  }
}
