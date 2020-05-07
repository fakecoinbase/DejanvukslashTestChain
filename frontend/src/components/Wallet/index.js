import React, {Component} from 'react';

import BASE_URL from '../../Constants';

import { Link } from 'react-router-dom';

import { withRouter } from 'react-router-dom';

import './Wallet.css';

class Wallet extends Component {
    _jsMounted = false;

    constructor(props) {
        super(props);
        this.state = {
            wallet : null
        }
    }

    componentDidMount() {
        this._jsMounted = true;
        this.fetchRandomWallet();
    }

    componentWillUnmount() {
        this._jsMounted = false;
    }

    async fetchRandomWallet() {
        await fetch('http://localhost:8080/wallet', {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        }).then(response => response.json()).then(wallet => {
            if(this._jsMounted) this.setState({ wallet });
        });
    }

    render() {

        const { wallet } = this.state;

        console.log(wallet);

        if(wallet != null) {
            return (
                <div className="wallet">
                    <h3> Private key: { wallet.privateKey } </h3>
                    <h3> Public key: { wallet.publicKey } </h3>
                </div>
            );
        }
        else return (
            <div>LOADING</div>
        );
    }
}

export default withRouter(Wallet);