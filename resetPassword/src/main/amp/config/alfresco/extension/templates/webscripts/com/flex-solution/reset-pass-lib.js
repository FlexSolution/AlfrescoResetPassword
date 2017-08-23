const PASS_MIN_LENGTH = 3;

function sendCallback(code, message) {
    status.code = code;
    status.message = message;
    status.redirect = true;
}

function isValid(pass, pass2){
    if(PASS_MIN_LENGTH > pass.length){
        return false;
    }

    return pass.equals(pass2);
}