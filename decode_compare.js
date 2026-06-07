const z = require('zlib');

const a_b64 = 'H4sIAAAAAAAAA83WzWrDMAwA4KfRYYcOSf6Tj0sHK+0CKx2MHhuavf8jzHLGpkBz8ckQjOJEbvxFsQthegI3A81MQPeRUVLS6IvQOc8RSzxM2nM5MmHaEeZyEl8Z2SGjKyfH07G0FM8u4jOiplw/9trl7W20ShpetOtyKS2fgw45Ro1H/S03cmn9qDeG0Wv/kLR/EI0v+gh7mDNI0mMWmDzc7hrcZrgxzAHkDjnU4BsmTaY9IunD8VVTswOhmprg9q2Dn+rV0+vh/W1xKHNBgjAfDvRQCF3MRFWIjRCxFfJWiEU6F+JGId4UqgUzTM4KkRXSuR3Pn4tQ6h3INQK5bSCpQH4BKq9gxx4tULQlRMKdC/lGIb8tFKpQsEKrEkpWKPUOFBqBwkOgMvsoSwlFC7RahWS1ClHsXCg2CsXHJYSSf3eyZIWcFcrrj6z3nSw1CqVtoWUnEyvkjVAd87+GQu9fmTQKyfYytAhlKxSsEK2EHPrOiXIjUX5MxOL8shDV1D+jaI3YGnnXeRURtv5jLFdmQP8DkbH4pmILAAA=';
const b_b64 = 'H4sIAAAAAAAEAM3WS27CMBAA0NPMoguq+fi7bEAqgkYqSqWKJRHh/keox6FVKmIWXllCZux4THhMbMCOLyAT0MQEdO0Zg/cafROKGHaY4m7UkeHAhH5DGFPH7RhZkJlT53A8pJbcSRy+ImoKnT+3+mbu81DyjH+97k3bYUgtn6wu2juNe/006XVl0+tM2xsd77yOd0HjQW9iC1OE4PU1BRgNXK4aXCa4MEwWwhWizcENRk2mLSLp7fFZU6NAoJzq4XLTxY/56nG3/3ifJdK3QQt22u9p1QjFRaJsxAsj4qIRh9C8EVcacdEoF003ytKIHoxOX7ORb59IKomkTBQykZmJ0o+wYYPFMqLAzRuZSiNTNrLZyC6NHsvo18i3T2QriewqEaN3YS4jtyR6shuRa97IVRq59TLCEO+nml8ayZNHrf1TzVca+bLRfKqFpZEp15Ft/1kLlUahvB3NRnFpZMtGgqZ5pFiJFNeROIiZN6Sc+qfkikpGmq8kwtr/kOnKBGh+ABnGKI52CwAA';

Promise.all([
  new Promise((resolve, reject) => z.gunzip(Buffer.from(a_b64, 'base64'), (e, d) => resolve(decodeURIComponent(d.toString())))),
  new Promise((resolve, reject) => z.gunzip(Buffer.from(b_b64, 'base64'), (e, d) => resolve(decodeURIComponent(d.toString()))))
]).then(([str1, str2]) => {
  console.log('=== 字符串1（本次提供的第一段） ===');
  console.log(str1);
  console.log();
  console.log('=== 字符串2（本次提供的第二段） ===');
  console.log(str2);
  console.log();
  console.log('=== 是否完全相同：' + (str1 === str2 ? '✅ 是' : '❌ 否'));
});
