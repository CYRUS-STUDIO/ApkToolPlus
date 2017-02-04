/**********************************************************\
|                                                          |
| base64.h                                                 |
|                                                          |
| Base64 library for C.                                    |
|                                                          |
| Code Authors: Chen fei <cf850118@163.com>                |
|               Ma Bingyao <mabingyao@gmail.com>           |
| LastModified: Mar 3, 2015                                |
|                                                          |
\**********************************************************/

#ifndef BASE64_INCLUDED
#define BASE64_INCLUDED

#include <stdlib.h>

#ifdef  __cplusplus
extern "C" {
#endif

/**
 * Function: base64_encode
 * @data:    Data to be encoded
 * @len:     Length of the data to be encoded
 * Returns:  Encoded data or %NULL on failure
 *
 * Caller is responsible for freeing the returned buffer.
 */
char * base64_encode(const unsigned char * data, size_t len);

/**
 * Function: base64_decode
 * @data:    Data to be decoded
 * @out_len: Pointer to output length variable
 * Returns:  Decoded data or %NULL on failure
 *
 * Caller is responsible for freeing the returned buffer.
 */
 unsigned char * base64_decode(const char * data, size_t * out_len);

#ifdef  __cplusplus
}
#endif

#endif
