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
        //backgroundColor: '#92B6D5'
    };

    divStyleRight = {
        float: 'right',
        width: '50%',
        height: '300px',
        //backgroundColor: '#92B6D5'
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
                        
                    </div>
                </div>
                <Explorer></Explorer>
            </div>
        );
    }
}

export default withRouter(Home);