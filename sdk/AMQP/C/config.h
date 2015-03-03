/* config.h */

/* Define to 1 for a Win32 build. */
/* #undef AMQP_BUILD */

/* Define to 1 for a static Win32 build. */
/* #undef AMQP_STATIC */

/* Host operating system string */
#define AMQ_PLATFORM "linux-gnu"

/* Define to 1 to enable thread safety */
#define ENABLE_THREAD_SAFETY 1

/* Define to 1 if SSL/TLS is enabled. */
#define WITH_SSL 1

/* Define to `__inline__' or `__inline' if that's what the C compiler
   calls it, or to nothing if 'inline' is not supported under any name.  */
#ifndef __cplusplus
/* #undef inline */
#endif
