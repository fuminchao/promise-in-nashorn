
var System = Java.type('java.lang.System');
var Thread = Java.type('java.lang.Thread');

new Promise(function(r, j) {
    r(Promise.resolve("Step0"));
})
.then(function(result) {

    assertTrue("assertStep0", result == 'Step0');
    return 1;
})
.then(function(result) {

    assertTrue("assertStep1", result == 1);
    return Promise.all([Promise.resolve(2), Promise.resolve(3)]);
})
.then(function(results) {

    assertTrue("assertStep2", results[0] == 2);
    assertTrue("assertStep2", results[1] == 3);

    return results.reduce(function(a, b) { return a+b; }, 0);
})
.then(function(result) {

    System.out.println(result);
    assertTrue("assertStep3", result == 5);

    return Promise.race([new Promise(function(r) {

        Thread.sleep(1000000);
        r(7);

    }), Promise.resolve(6)]);
})
.then(function(result) {

    assertTrue("assertStep3", result == 6);

    return 99;
});
