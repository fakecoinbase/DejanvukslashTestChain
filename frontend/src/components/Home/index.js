import React, { Component } from 'react';

import Button from 'react-bootstrap/Button';

import Explorer from '../Explorer';

import { withRouter } from 'react-router-dom';


import './Home.css';

class Home extends Component {
    constructor(props) {
        super(props);
        this.state = {
            wallets: 0,
            transactions: 0,
            blocks: 0
        }
    }

    async componentDidMount() {

    }

    divStyleLeft = {
        float: 'left',
        width: '50%',
        height: '300px',
        backgroundColor: 'rgb(4, 13, 37)'
    };

    divStyleRight = {
        float: 'right',
        width: '50%',
        height: '300px',
        backgroundColor: 'rgb(20, 70, 153)'
    };

    render() {
        const { wallets, transactions, blocks } = this.state;

        return (
            <div>
                <div className="home">
                    <div style={this.divStyleLeft}>
                        <div className="row first">
                            <div className="col-sm-4"></div>
                            <div className="col-sm-4">
                                <Button className="homeButtons" id="btnHomeLeft" className="btnHome" onClick={() => { this.props.history.push('/tx/send'); }}>
                                Send Transaction
                            </Button>
                            </div>
                            <div className="col-sm-4"></div>
                        </div>
                    </div>
                    <div style={this.divStyleRight}>
                        <div className="row first">
                            <div className="col-sm-4"></div>
                            <div className="col-sm-4">
                                <Button className="homeButtons" id="btnHomeRight" className="btnHome" onClick={() => { this.props.history.push('/wallet'); }}>
                                    Create Wallet
                            </Button>
                            </div>
                            <div className="col-sm-4"></div>
                        </div>
                    </div>

                </div>
                <div className="statistics">
                    <div className="row">
                        <div className="col-sm-4 text-center center-block wallets-statistics">
                            <h5>Wallets</h5>
                            <h3>7000</h3>
                        </div>
                        <div className="col-sm-4 text-center center-block transactions-statistics">
                            <h5>Transactions</h5>
                            <h3>500M+</h3>
                        </div>
                        <div className="col-sm-4 text-center center-block blocks-statistics">
                            <h5>Blocks</h5>
                            <h3>10B+</h3>
                        </div>
                    </div>
                </div>
                <Explorer></Explorer>
            </div>
        );
    }
}

export default withRouter(Home);